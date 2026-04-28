package com.example.waterquality.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared date formatting utilities for composables.
 */
fun formatTimestamp(ts: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ts))

fun formatDateShort(ts: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ts))

fun timeAgo(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000L        -> "Just now"
        diff < 3_600_000L     -> "${diff / 60_000}m ago"
        diff < 86_400_000L    -> "${diff / 3_600_000}h ago"
        diff < 604_800_000L   -> "${diff / 86_400_000}d ago"
        else                  -> formatDateShort(ts)
    }
}
