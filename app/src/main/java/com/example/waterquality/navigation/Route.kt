package com.example.waterquality.navigation

/**
 * Type-safe route definitions for Navigation Compose.
 *
 * Each object/class becomes a unique string route key.
 * Using 'const val' route strings keeps it compatible with
 * standard NavHost (Navigation Compose 2.x) on all API levels.
 */
object Routes {
    const val SPLASH        = "splash"
    const val HOME          = "home"
    const val MAP           = "map"
    const val REPORT        = "report_submission"
    const val ADVISORIES    = "advisories"
    const val ALERTS        = "alerts"
    const val PROFILE       = "profile"
    const val REPORT_DETAIL = "report_detail/{reportId}"

    fun reportDetail(reportId: String) = "report_detail/$reportId"
}

/** Bottom-nav tabs — only the screens that appear in the nav bar. */
enum class BottomNavTab(
    val route: String,
    val label: String,
    val iconRes: Int? = null   // We use Icons.* in code; kept null here for simplicity
) {
    HOME       (Routes.HOME,       "Home"),
    MAP        (Routes.MAP,        "Map"),
    ADVISORIES (Routes.ADVISORIES, "Cards"),
    ALERTS     (Routes.ALERTS,     "Alerts"),
    PROFILE    (Routes.PROFILE,    "Profile")
}
