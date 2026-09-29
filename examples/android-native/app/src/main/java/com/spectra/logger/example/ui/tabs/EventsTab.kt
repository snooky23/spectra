package com.spectra.logger.example.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spectra.logger.example.BuildConfig
import com.spectra.logger.example.ui.components.BrandingCard
import com.spectra.logger.example.ui.components.LogButton
import com.spectra.logger.example.ui.components.OpenSpectraButton
import com.spectra.logger.example.ui.components.SectionHeader
import com.spectra.logger.example.viewmodel.EventsViewModel

@Composable
fun EventsTab(
    viewModel: EventsViewModel,
    onOpenSpectra: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            BrandingCard(
                icon = Icons.Default.Timeline,
                title = "Events Testing",
                subtitle = "Track screen views, user actions & telemetry"
            )
        }

        item { SectionHeader("Screen View Tracking") }

        item {
            LogButton(
                label = if (viewModel.isTrackingProductDetails) "⏹ End 'ProductDetails' Screen" else "▶ Start 'ProductDetails' Screen",
                icon = if (viewModel.isTrackingProductDetails) Icons.Default.Stop else Icons.Default.PlayArrow,
                backgroundColor = if (viewModel.isTrackingProductDetails) Color(0xFFF44336) else Color(0xFF4CAF50),
                action = viewModel::toggleProductDetailsScreenView
            )
        }

        item {
            Text(
                text = if (viewModel.isTrackingProductDetails) "Dwell timer active... Tap to stop and record durationMs" else "Starts duration timer until you tap again",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            LogButton(
                label = "Simulate 'Cart' Screen View (1.5s Dwell)",
                icon = Icons.Default.Visibility,
                backgroundColor = Color(0xFF2196F3),
                action = viewModel::simulateQuickScreenView
            )
        }

        item { SectionHeader("User Actions") }

        item {
            LogButton(
                label = "Add to Cart (with SKU & Price)",
                icon = Icons.Default.ShoppingCart,
                backgroundColor = Color(0xFFE91E63),
                action = viewModel::logAddToCart
            )
        }

        item {
            LogButton(
                label = "Like Post (with Reaction param)",
                icon = Icons.Default.Favorite,
                backgroundColor = Color(0xFFE91E63),
                action = viewModel::logLikePost
            )
        }

        item {
            LogButton(
                label = "Submit Search Query",
                icon = Icons.Default.Search,
                backgroundColor = Color(0xFF9C27B0),
                action = viewModel::logSearchSubmitted
            )
        }

        item { SectionHeader("Lifecycle Events") }

        item {
            LogButton(
                label = "Simulate App Backgrounded",
                icon = Icons.Default.PauseCircle,
                backgroundColor = Color(0xFFFF9800),
                action = viewModel::logAppBackground
            )
        }

        item {
            LogButton(
                label = "Simulate App Foregrounded",
                icon = Icons.Default.PlayCircle,
                backgroundColor = Color(0xFFFF9800),
                action = viewModel::logAppForeground
            )
        }

        item {
            LogButton(
                label = "Simulate Low Memory Warning",
                icon = Icons.Default.Warning,
                backgroundColor = Color(0xFFFF5722),
                action = viewModel::logLowMemoryWarning
            )
        }

        item { SectionHeader("Custom Business Events") }

        item {
            LogButton(
                label = "Purchase Completed ($249.99)",
                icon = Icons.Default.CheckCircle,
                backgroundColor = Color(0xFF009688),
                action = viewModel::logPurchaseCompleted
            )
        }

        item {
            LogButton(
                label = "Feature Flag Evaluated",
                icon = Icons.Default.Tune,
                backgroundColor = Color(0xFF607D8B),
                action = viewModel::logFeatureFlagEvaluated
            )
        }

        item { SectionHeader("Batch Generation") }

        item {
            LogButton(
                label = "Generate 20 Mixed Events",
                icon = Icons.AutoMirrored.Filled.List,
                backgroundColor = Color(0xFF3F51B5),
                action = viewModel::generate20MixedEvents
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (BuildConfig.DEBUG) {
            item {
                OpenSpectraButton(onClick = onOpenSpectra)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
