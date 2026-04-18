package com.spectra.logger.ui.compose.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun <T> AdaptiveNavigator(
    modifier: Modifier = Modifier,
    listContent: @Composable (
        navigateToDetail: (T) -> Unit
    ) -> Unit,
    detailContent: @Composable (
        selectedItem: T,
        navigateBack: () -> Unit
    ) -> Unit
) {
    var selectedItem by remember { mutableStateOf<T?>(null) }
    val screenConfig = com.spectra.logger.ui.theme.rememberScreenConfig()

    // Branch navigation based on layout context
    if (screenConfig.isDualPane) {
        // Medium/Expanded Device: Dual-pane fallback layout. 
        // Epic 2 will fully enhance this with native ListDetailPane scaffolds.
        Row(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.4f)) {
                listContent(
                    { item -> selectedItem = item }
                )
            }
            Box(modifier = Modifier.weight(0.6f)) {
                val current = selectedItem
                if (current != null) {
                    detailContent(
                        current,
                        { selectedItem = null }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize())
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
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> -fullWidth / 2 }
                    )
                } else {
                    // Navigating back to list
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> -fullWidth / 2 }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> fullWidth }
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { currentItem ->
            if (currentItem == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    listContent(
                        { item ->
                            selectedItem = item
                        }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    detailContent(
                        currentItem,
                        { selectedItem = null }
                    )
                }
            }
        }
    }
}
