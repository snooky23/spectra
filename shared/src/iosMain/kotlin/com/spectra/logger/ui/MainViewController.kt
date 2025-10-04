package com.spectra.logger.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.spectra.logger.domain.storage.LogStorage
import com.spectra.logger.domain.storage.NetworkLogStorage
import com.spectra.logger.ui.screens.LogListScreen
import com.spectra.logger.ui.screens.NetworkLogScreen
import platform.UIKit.UIViewController

/**
 * Creates a UIViewController containing the LogListScreen.
 * This is exposed to Swift/Objective-C for iOS integration.
 */
fun createLogListViewController(storage: LogStorage): UIViewController {
    return ComposeUIViewController {
        LogListScreen(storage = storage)
    }
}

/**
 * Creates a UIViewController containing the NetworkLogScreen.
 * This is exposed to Swift/Objective-C for iOS integration.
 */
fun createNetworkLogViewController(storage: NetworkLogStorage): UIViewController {
    return ComposeUIViewController {
        NetworkLogScreen(storage = storage)
    }
}
