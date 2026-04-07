package com.spectra.logger.ui

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/**
 * iOS implementation of the Spectra Logger UI manager.
 */
actual object SpectraUIManager {
    private var controllerProvider: (() -> UIViewController)? = null
    private var currentController: UIViewController? = null

    /**
     * Registers a provider for the Spectra Logger UIViewController.
     * This is typically called by the UI module during initialization.
     */
    fun registerControllerProvider(provider: () -> UIViewController) {
        this.controllerProvider = provider
    }

    actual fun showScreen() {
        if (currentController != null) return

        val provider = controllerProvider ?: return
        val controller = provider()
        currentController = controller

        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        var top = root
        while (top.presentedViewController != null) {
            top = top.presentedViewController!!
        }

        top.presentViewController(controller, animated = true, completion = null)
    }

    actual fun dismissScreen() {
        currentController?.dismissViewControllerAnimated(true, completion = null)
        currentController = null
    }
}
