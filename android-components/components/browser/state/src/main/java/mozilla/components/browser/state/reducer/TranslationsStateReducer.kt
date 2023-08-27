/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.state.reducer

import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TranslationsState

internal object TranslationsStateReducer {
    fun reduce(state: BrowserState, action: TranslationsAction): BrowserState = when (action) {
        is TranslationsAction.UpdateTranslationsAvailable -> state.copyWithTranslationsState(action.tabId) {
            it.copy(isTranslationsAvailable = action.isTranslationsAvailable)
        }
        is TranslationsAction.UpdateLanguageState -> state.copyWithTranslationsState(action.tabId) {
            it.copy(
                requestedTranslationPair = action.requestedTranslationPair,
                detectedLanguages = action.detectedLanguages,
                error = action.error,
                isEngineReady = action.isEngineReady,
            )
        }
    }
}

private inline fun BrowserState.copyWithTranslationsState(
    tabId: String,
    crossinline update: (TranslationsState) -> TranslationsState,
): BrowserState {
    return updateTabOrCustomTabState(tabId) { current ->
        current.createCopy(translationsState = update(current.translationsState))
    }
}
