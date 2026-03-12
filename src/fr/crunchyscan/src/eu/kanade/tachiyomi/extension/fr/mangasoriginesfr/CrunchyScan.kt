package eu.kanade.tachiyomi.extension.fr.crunchyscan

import eu.kanade.tachiyomi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class CrunchyScan :
    Madara(
        "CrunchyScan",
        "https://crunchyscan.fr",
        "fr",
        SimpleDateFormat("MMMM d, yyyy", Locale("fr")),
    )
