package eu.kanade.tachiyomi.extension.fr.scanmanga

import eu.kanade.tachiyomi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ScanManga :
    Madara(
        "ScanManga",
        "https://www.scan-manga.com/?po",
        "fr",
        SimpleDateFormat("MMMM d, yyyy", Locale("fr")),
    )
