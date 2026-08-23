package eu.kanade.tachiyomi.extension.fr.mangasorigines

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

class MangaOrigines : ParsedHttpSource() {

    override val name = "Manga Origines"
    override val baseUrl = "https://mangas-origines.fr"
    override val lang = "fr"
    override val supportsLatest = true

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .header("Referer", "$baseUrl/")
                .build()
            chain.proceed(request)
        }
        .build()

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
        .add("Referer", "$baseUrl/")

    // --- POPULAIRES ---
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/catalogue?page=$page", headers)
    override fun popularMangaSelector(): String = "div.grid div.group, div.manga-card, div.page-item-detail"
    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        val titleEl = element.select("h3 a, a.manga-title, div.post-title a").first()!!
        title = titleEl.text().trim()
        setUrlWithoutDomain(titleEl.attr("href"))
        thumbnail_url = element.select("img").attr("abs:data-src").ifEmpty {
            element.select("img").attr("abs:src")
        }
    }
    override fun popularMangaNextPageSelector(): String? = "a[rel=next], .pagination .next, a:contains(Suivant)"

    // --- DERNIÈRES SORTIES ---
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/dernieres-sorties?page=$page", headers)
    override fun latestUpdatesSelector(): String = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector(): String? = popularMangaNextPageSelector()

    // --- RECHERCHE ---
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/recherche?q=${java.net.URLEncoder.encode(query, "UTF-8")}&page=$page", headers)
    override fun searchMangaSelector(): String = popularMangaSelector()
    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector(): String? = null

    // --- DÉTAILS ---
    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        description = document.select("div.synopsis, div.description, div.summary__content").text().trim()
        genre = document.select("div.genres a, div.genres-content a").joinToString { it.text().trim() }
        status = when (document.select("div.post-status, div.status").text().lowercase()) {
            "en cours", "ongoing" -> SManga.ONGOING
            "terminé", "completed" -> SManga.COMPLETED
            else -> SManga.UNKNOWN
        }
        thumbnail_url = document.select("div.summary_image img, div.manga-poster img").attr("abs:data-src").ifEmpty {
            document.select("div.summary_image img, div.manga-poster img").attr("abs:src")
        }
    }

    // --- CHAPITRES ---
    override fun chapterListSelector(): String = "li.wp-manga-chapter, div.chapter-item, ul.chapters-list li"
    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        val link = element.select("a").first()!!
        setUrlWithoutDomain(link.attr("href"))
        name = link.text().trim()
        date_upload = parseDate(element.select("span.chapter-release-date, span.date").text().trim())
    }

    private fun parseDate(dateStr: String): Long {
        return runCatching {
            SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(dateStr)?.time
                ?: SimpleDateFormat("d MMMM yyyy", Locale.FRENCH).parse(dateStr)?.time
                ?: 0L
        }.getOrDefault(0L)
    }

    // --- EXTRACTION DES PAGES ---
    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        document.select("div.page-break img, div.reading-content img, div#chapter-images img").forEachIndexed { index, element ->
            val url = element.attr("data-src").ifEmpty {
                element.attr("data-lazy-src").ifEmpty {
                    element.attr("src")
                }
            }.trim()
            if (url.isNotBlank()) {
                pages.add(Page(index, "", url))
            }
        }
        return pages
    }

    override fun imageUrlParse(document: Document): String = ""
}
