/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.concept.engine.translations

/**
 * https://searchfox.org/mozilla-central/source/toolkit/components/translations/actors/TranslationsParent.sys.mjs#154
 */
data class TranslationPair(
    val fromLanguage: String,
    val toLanguage: String,
    val fromDisplayLanguage: String? = null,
    val toDisplayLanguage: String? = null,
)
