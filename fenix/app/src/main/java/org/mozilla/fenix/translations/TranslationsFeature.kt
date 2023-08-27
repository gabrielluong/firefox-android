/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.translations

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature

/**
 * TODO.
 */
class TranslationsFeature(
    private val store: BrowserStore,
    private val onAvailabilityChange: (isAvailable: Boolean) -> Unit,
) : LifecycleAwareFeature {
    private var scope: CoroutineScope? = null

    override fun start() {
        scope = store.flowScoped { flow ->
            flow.mapNotNull { it.selectedTab }
                .map { it.translationsState }
                .distinctUntilChanged()
                .collect { state ->
                    onAvailabilityChange(state.isTranslationsAvailable)
                }
        }
    }

    override fun stop() {
        scope?.cancel()
    }
}
