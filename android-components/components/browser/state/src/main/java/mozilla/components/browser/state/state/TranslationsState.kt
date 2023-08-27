/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.state.state

import mozilla.components.concept.engine.translations.LangTags
import mozilla.components.concept.engine.translations.TranslationPair

/**
 * https://searchfox.org/mozilla-central/source/toolkit/components/translations/actors/TranslationsParent.sys.mjs#2342
 */
data class TranslationsState(
    val isTranslationsAvailable: Boolean = false,
    val requestedTranslationPair: TranslationPair? = null,
    val detectedLanguages: LangTags? = LangTags(),
    val error: String? = null,
    val isEngineReady: Boolean = false,
)
