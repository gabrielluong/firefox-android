/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.concept.engine.translations

/**
 * Used to decide how to translate a page for full page translations.
 *
 * https://searchfox.org/mozilla-central/source/toolkit/components/translations/translations.d.ts#284
 */
data class LangTags(
    val isDocLangTagSupported: Boolean = false,
    val docLangTag: String? = null,
    val userLangTag: String? = null,
)
