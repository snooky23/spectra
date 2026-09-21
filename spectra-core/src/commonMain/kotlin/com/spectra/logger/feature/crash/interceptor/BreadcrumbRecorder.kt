package com.spectra.logger.feature.crash.interceptor

import com.spectra.logger.feature.crash.model.Breadcrumb
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

interface BreadcrumbRecorder {
    fun record(breadcrumb: Breadcrumb)

    fun getBreadcrumbs(): List<Breadcrumb>

    fun clear()
}

class DefaultBreadcrumbRecorder(
    private val maxCapacity: Int = DEFAULT_CAPACITY,
) : BreadcrumbRecorder {
    private val lock = SynchronizedObject()
    private val buffer = ArrayDeque<Breadcrumb>(maxCapacity)

    override fun record(breadcrumb: Breadcrumb) {
        synchronized(lock) {
            if (buffer.size >= maxCapacity) {
                buffer.removeFirst()
            }
            buffer.addLast(breadcrumb)
        }
    }

    override fun getBreadcrumbs(): List<Breadcrumb> =
        synchronized(lock) {
            buffer.toList()
        }

    override fun clear() {
        synchronized(lock) {
            buffer.clear()
        }
    }

    companion object {
        const val DEFAULT_CAPACITY = 50
    }
}
