plugins {
    id("com.android.application")
    id("kotlin-android")
}

ext {
    extName = "Scan-Manga"
    pkgNameSuffix = "fr.scanmanga"
    extClass = ".ScanManga"
    extVersionCode = 1
    libVersion = "1.4"
}

apply(from = "$rootDir/common.gradle")
