package com.example.waterquality.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * ParallaxCard — wraps content so the inner layer can be offset
 * independently from the outer frame, creating a depth/parallax effect.
 *
 * HOW IT WORKS:
 * The outer [Box] is the card boundary (static position on screen).
 * The [content] lambda receives a [parallaxOffset] float (pixels).
 * Callers apply this offset to the background/image layer via
 * `Modifier.graphicsLayer { translationY = -parallaxOffset }`.
 *
 * The [parallaxOffset] is computed from [listState] + [itemIndex]:
 *   fraction = viewportOffset / viewportHeight  (0..1)
 *   offset   = fraction * [depth]
 *
 * Works on API 24+ — pure Compose, no hardware-specific APIs.
 *
 * Example usage inside LazyColumn:
 * ```
 * ParallaxCard(
 *     listState = listState,
 *     itemIndex = index,
 *     modifier  = Modifier.fillMaxWidth().height(180.dp)
 * ) { offset ->
 *     Box(
 *         Modifier
 *             .fillMaxSize()
 *             .graphicsLayer { translationY = -offset }
 *             .background(gradient)
 *     )
 *     Text(content)
 * }
 * ```
 */
@Composable
fun ParallaxCard(
    listState: LazyListState,
    itemIndex: Int,
    modifier:  Modifier = Modifier,
    depth:     Float    = 24f,          // max parallax pixels
    content:   @Composable BoxScope.(parallaxOffset: Float) -> Unit
) {
    // Compute how far this item is from the top of the viewport
    val layoutInfo = listState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo

    val itemInfo = visibleItems.firstOrNull { it.index == itemIndex }
    val parallaxOffset: Float = if (itemInfo != null) {
        val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
        if (viewportHeight == 0f) 0f
        else {
            val itemCenter = itemInfo.offset + itemInfo.size / 2f
            val fraction   = (itemCenter / viewportHeight).coerceIn(0f, 1f)
            (fraction - 0.5f) * 2f * depth   // -depth .. +depth
        }
    } else 0f

    Box(modifier = modifier) {
        content(parallaxOffset)
    }
}

/**
 * Parallax offset for a HorizontalPager card.
 * Pass the current [pageOffset] fraction (from PagerState.currentPageOffsetFraction)
 * and the card's [pageIndex] relative to currentPage.
 *
 * Usage inside a pager:
 * ```
 * val offset = pagerState.currentPageOffsetFraction
 * graphicsLayer {
 *     translationX = pagerParallaxOffset(offset, page - pagerState.currentPage)
 * }
 * ```
 */
fun pagerParallaxOffset(pageOffset: Float, relativeIndex: Int, depth: Float = 40f): Float {
    val fraction = pageOffset + relativeIndex
    return fraction * depth
}
