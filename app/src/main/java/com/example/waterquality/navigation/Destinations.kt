package com.example.waterquality.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Map : Destination

    @Serializable
    data object ReportSubmission : Destination

    @Serializable
    data object Advisories : Destination

    @Serializable
    data class ReportDetails(val reportId: String) : Destination
}
