package com.example.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A highly responsive and adaptive layout utility suite for Gupta Lakhani & Associates.
 * Configured to dynamically scale margins, paddings, and content organization across
 * varied phone aspect ratios (Android and iOS), compact heights (landscape), and wider
 * screen surfaces (tablets, iPads, and foldables).
 */

object ResponsiveLayout {

    /**
     * Determines if the current screen configuration has a compact width (e.g., small phones).
     */
    @Composable
    fun isCompactWidth(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp < 360
    }

    /**
     * Determines if the current screen configuration is exceptionally wide (e.g., tablets, iPads, landscape phones).
     */
    @Composable
    fun isExpandedWidth(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp >= 600
    }

    /**
     * Determines if the current screen configuration has a compact height (e.g., landscape orientation or small iPhones/SE).
     */
    @Composable
    fun isCompactHeight(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenHeightDp < 540
    }

    /**
     * Provides dynamic spacing based on the current width constraint.
     */
    @Composable
    fun responsivePadding(): Dp {
        val configuration = LocalConfiguration.current
        return when {
            configuration.screenWidthDp >= 600 -> 24.dp // Large screen
            configuration.screenWidthDp >= 360 -> 16.dp // Standard screen (e.g., iPhone/Android)
            else -> 12.dp                               // Compact screen (e.g., iPhone SE, compact Android)
        }
    }

    /**
     * A layout wrapper that constraints content to a beautifully centered visual bounds on wide screens
     * (e.g., iPads, foldables, tablets, horizontal phone views) but allows full fluid bleed on standard mobile screens.
     */
    @Composable
    fun ResponsiveContainer(
        modifier: Modifier = Modifier,
        maxWidth: Dp = 600.dp,
        contentAlignment: Alignment = Alignment.TopCenter,
        content: @Composable BoxScope.() -> Unit
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = contentAlignment
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
                content = content
            )
        }
    }

    /**
     * A responsive page container that automatically applies proper screen boundaries, safe areas,
     * optional scrolling, and horizontal limit constraints to make the page extremely legible on any screen aspect ratio.
     */
    @Composable
    fun ResponsivePage(
        modifier: Modifier = Modifier,
        isScrollable: Boolean = true,
        scrollState: ScrollState = rememberScrollState(),
        maxWidth: Dp = 600.dp,
        horizontalPadding: Dp = responsivePadding(),
        verticalPadding: Dp = 12.dp,
        verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        content: @Composable ColumnScope.() -> Unit
    ) {
        ResponsiveContainer(
            modifier = modifier,
            maxWidth = maxWidth
        ) {
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .let {
                    if (isScrollable) it.verticalScroll(scrollState) else it
                }

            Column(
                modifier = contentModifier,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                content = content
            )
        }
    }

    /**
     * A crucial responsive component that acts as a Row on wider displays (e.g. tablets, iPads, landscape)
     * and as a Column on compact displays (narrow iPhones, portrait).
     * Extremely useful for splitting forms, side-by-side action buttons, and details blocks.
     */
    @Composable
    fun AdaptiveRowOrColumn(
        modifier: Modifier = Modifier,
        forceColumn: Boolean = false,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
        horizontalAlignment: Alignment.Horizontal = Alignment.Start,
        content: @Composable @ExtensionFunctionType RowOrColumnScope.() -> Unit
    ) {
        val useRow = !forceColumn && isExpandedWidth()

        if (useRow) {
            Row(
                modifier = modifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment
            ) {
                val scope = RowOrColumnScopeImpl(this, null)
                scope.content()
            }
        } else {
            Column(
                modifier = modifier,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment
            ) {
                val scope = RowOrColumnScopeImpl(null, this)
                scope.content()
            }
        }
    }

    /**
     * Scope interface allowing unified weight assignment regardless of whether the parent is currently a Row or Column.
     */
    interface RowOrColumnScope {
        @Composable
        fun Modifier.weight(weight: Float, fill: Boolean = true): Modifier
    }

    private class RowOrColumnScopeImpl(
        val rowScope: RowScope?,
        val columnScope: ColumnScope?
    ) : RowOrColumnScope {
        @Composable
        override fun Modifier.weight(weight: Float, fill: Boolean): Modifier {
            return when {
                rowScope != null -> with(rowScope) { this@weight.weight(weight, fill) }
                columnScope != null -> with(columnScope) { this@weight.weight(weight, fill) }
                else -> this
            }
        }
    }
}
