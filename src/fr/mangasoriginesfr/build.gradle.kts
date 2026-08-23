plugins {
    id("com.android.application")
}

ext {
    extName = "Manga Origines"
    pkgNameSuffix = "fr.mangasorigines"
    extClass = ".MangaOrigines"
    extVersionCode = 1
    libVersion = "1.4"
}

apply(from = "$rootDir/common.gradle")
