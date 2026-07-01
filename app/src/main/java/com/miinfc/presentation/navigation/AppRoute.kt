package com.miinfc.presentation.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object ImportKey : AppRoute("import-key")
    data object ImportBin : AppRoute("import-bin")
    data object Scan : AppRoute("scan")
    data object Write : AppRoute("write")
    data object Diagnostic : AppRoute("diagnostic")
    data object KeyGuide : AppRoute("guide-key")
    data object BinGuide : AppRoute("guide-bin")
    data object WriteGuide : AppRoute("guide-write")
    data object AmiiboInfo : AppRoute("amiibo-info")
    data object Collection : AppRoute("collection")
    data object Settings : AppRoute("settings")
}
