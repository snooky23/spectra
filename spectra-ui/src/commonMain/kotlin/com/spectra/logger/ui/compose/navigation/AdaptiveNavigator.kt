package com.spectra.logger.ui.compose.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spectra.logger.ui.theme.SpectraDesignTokens

@Composable
fun <T> AdaptiveNavigator(
    modifier: Modifier = Modifier,
    listContent: @Composable (
        navigateToDetail: (T) -> Unit,
        isDualPane: Boolean,
    ) -> Unit,
    detailContent: @Composable (
        selectedItem: T,
        navigateBack: () -> Unit,
        isDualPane: Boolean,
    ) -> Unit,
    emptyDetailContent: @Composable (() -> Unit)? = null,
) {
    var selectedItem by remember { mutableStateOf<T?>(null) }
    val screenConfig = com.spectra.logger.ui.theme.rememberScreenConfig()

    // Branch navigation based on layout context
    if (screenConfig.isDualPane) {
        // Medium/Expanded Device: Persistent dual-pane side-by-side layout.
        Row(modifier = modifier.fillMaxSize()) {
            // List pane — 40% of width
            Box(modifier = Modifier.weight(0.4f)) {
                listContent(
                    { item -> selectedItem = item },
                    true,
                )
            }
            // Vertical divider between panes
            HorizontalDivider(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(SpectraDesignTokens.ListToDetailGap),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            // Detail pane — 60% of width
            Box(modifier = Modifier.weight(0.6f)) {
                val current = selectedItem
                if (current != null) {
                    detailContent(
                        current,
                        { selectedItem = null },
                        true,
                    )
                } else {
                    // Empty state: show placeholder or custom content
                    if (emptyDetailContent != null) {
                        emptyDetailContent()
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Select an item to view details",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Compact Device: Pushes a new full-screen view onto the navigation stack
        AnimatedContent(
            targetState = selectedItem,
            transitionSpec = {
                if (targetState != null) {
                    // Navigating to detail
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> fullWidth },
                    ) togetherWith
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> -fullWidth / 2 },
                        )
                } else {
                    // Navigating back to list
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> -fullWidth / 2 },
                    ) togetherWith
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> fullWidth },
                        )
                }
            },
            modifier = modifier.fillMaxSize(),
        ) { currentItem ->
            if (currentItem == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    listContent(
                        { item -> selectedItem = item },
                        false,
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    detailContent(
                        currentItem,
                        { selectedItem = null },
                        false,
                    )
                }
            }
        }
    }
}
