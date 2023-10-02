/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.awesomebar.provider

import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.storage.HistoryMetadata
import mozilla.components.concept.storage.HistoryMetadataStorage
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.ktx.android.net.sameHostWithoutMobileSubdomainAs
import java.util.UUID

/**
 * Return 5 history suggestions by default.
 */
const val DEFAULT_COMBINED_SUGGESTION_LIMIT = 5

/**
 * Default suggestions limit multiplier when needing to filter results by an external url filter.
 */
@VisibleForTesting
internal const val COMBINED_HISTORY_RESULTS_TO_FILTER_SCALE_FACTOR = 10

/**
 * A [AwesomeBar.SuggestionProvider] implementation that combines suggestions from
 * [HistoryMetadataSuggestionProvider] and [HistoryStorageSuggestionProvider].
 * It will return suggestions using [HistoryMetadataSuggestionProvider] first,
 * followed by suggestion from [HistoryStorageSuggestionProvider] up to the provided
 * [maxNumberOfSuggestions].
 *
 * @property historyStorage an instance of the [HistoryStorage] used
 * to query matching metadata records.
 * @property historyMetadataStorage an instance of the [HistoryStorage] used
 * to query matching metadata records.
 * @property loadUrlUseCase the use case invoked to load the url when the
 * user clicks on the suggestion.
 * @property icons optional instance of [BrowserIcons] to load fav icons
 * for [HistoryMetadata] URLs.
 * @property engine optional [Engine] instance to call [Engine.speculativeConnect] for the
 * highest scored suggestion URL.
 * @property maxNumberOfSuggestions optional parameter to specify the maximum number of returned suggestions,
 * defaults to [DEFAULT_COMBINED_SUGGESTION_LIMIT].
 * @property showEditSuggestion optional parameter to specify if the suggestion should show the edit button.
 * @property suggestionsHeader optional parameter to specify if the suggestion should have a header.
 * @property resultsUriFilter Optional filter for the host url of the suggestions to show.
 */
@Suppress("LongParameterList")
class CombinedHistorySuggestionProvider(
    private val historyStorage: HistoryStorage,
    private val historyMetadataStorage: HistoryMetadataStorage,
    private val loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
    private val icons: BrowserIcons? = null,
    internal val engine: Engine? = null,
    internal var maxNumberOfSuggestions: Int = DEFAULT_COMBINED_SUGGESTION_LIMIT,
    @get:VisibleForTesting val showEditSuggestion: Boolean = true,
    private val suggestionsHeader: String? = null,
    @get:VisibleForTesting val resultsUriFilter: Uri? = null,
) : AwesomeBar.SuggestionProvider {
    override val id: String = UUID.randomUUID().toString()

    override fun groupTitle(): String? {
        return suggestionsHeader
    }

    override suspend fun onInputChanged(text: String): List<AwesomeBar.Suggestion> = coroutineScope {
        historyStorage.cancelReads(text)
        historyMetadataStorage.cancelReads(text)

        if (text.isBlank()) {
            return@coroutineScope emptyList()
        }

        val metadataSuggestionsAsync = async {
            when (resultsUriFilter) {
                null -> getMetadataSuggestions(text)
                else -> getMetadataSuggestionsFromHost(resultsUriFilter, text)
            }
        }

        val historySuggestionsAsync = async {
            when (resultsUriFilter) {
                null -> getHistorySuggestions(text)
                else -> getHistorySuggestionsFromHost(resultsUriFilter, text)
            }
        }

        val metadataSuggestions = metadataSuggestionsAsync.await()
        val historySuggestions = historySuggestionsAsync.await()
        val historyTopScore = historySuggestions.firstOrNull()?.score
        val updatedMetadataSuggestions = if (historyTopScore != null) {
            // Make sure history metadata suggestions have a higher score than regular history
            // suggestions but otherwise retain their relative order.
            val size = metadataSuggestions.size
            metadataSuggestions.mapIndexed { index, suggestion ->
                suggestion.copy(score = (size - index) + historyTopScore)
            }
        } else {
            metadataSuggestions
        }

        val combinedSuggestions = (updatedMetadataSuggestions + historySuggestions)
            .distinctBy { it.description }
            .take(maxNumberOfSuggestions)

        combinedSuggestions.firstOrNull()?.description?.let { url -> engine?.speculativeConnect(url) }

        return@coroutineScope combinedSuggestions
    }

    /**
     * Set maximum number of suggestions.
     */
    fun setMaxNumberOfSuggestions(maxNumber: Int) {
        if (maxNumber <= 0) {
            return
        }

        maxNumberOfSuggestions = maxNumber
    }

    /**
     * Get the maximum number of suggestions that will be provided.
     */
    @VisibleForTesting
    fun getMaxNumberOfSuggestions() = maxNumberOfSuggestions

    /**
     * Reset maximum number of suggestions to default.
     */
    fun resetToDefaultMaxSuggestions() {
        maxNumberOfSuggestions = DEFAULT_COMBINED_SUGGESTION_LIMIT
    }

    /**
     * Get up to [maxNumberOfSuggestions] history metadata suggestions matching [query].
     *
     * @param query String to filter bookmarks' title or URL by.
     */
    private suspend fun getMetadataSuggestions(query: String) = historyMetadataStorage
        .queryHistoryMetadata(query, maxNumberOfSuggestions)
        .filter { it.totalViewTime > 0 }
        .into(this@CombinedHistorySuggestionProvider, icons, loadUrlUseCase, showEditSuggestion)

    /**
     * Get up to [maxNumberOfSuggestions] history metadata suggestions matching [query] from the indicated [url].
     *
     * @param url URL host to filter all history entry's URL host by.
     * @param query String to filter history entry's title or URL by.
     */
    private suspend fun getMetadataSuggestionsFromHost(url: Uri, query: String) = historyMetadataStorage
        .queryHistoryMetadata(query, maxNumberOfSuggestions * COMBINED_HISTORY_RESULTS_TO_FILTER_SCALE_FACTOR)
        .filter {
            it.totalViewTime > 0 && it.key.url.toUri().sameHostWithoutMobileSubdomainAs(url)
        }
        .take(maxNumberOfSuggestions)
        .into(this@CombinedHistorySuggestionProvider, icons, loadUrlUseCase, showEditSuggestion)

    /**
     * Get up to [maxNumberOfSuggestions] history suggestions matching [query].
     *
     * @param query String to filter history entry's title or URL by.
     */
    private suspend fun getHistorySuggestions(query: String) = historyStorage
        .getSuggestions(query, maxNumberOfSuggestions)
        .sortedByDescending { it.score }
        .distinctBy { it.id }
        .into(this@CombinedHistorySuggestionProvider, icons, loadUrlUseCase, showEditSuggestion)

    /**
     * Get up to [maxNumberOfSuggestions] history metadata suggestions matching [query] from the indicated [url].
     *
     * @param url URL host to filter all bookmarks' URL host by.
     * @param query String to filter history entry's title or URL by.
     */
    private suspend fun getHistorySuggestionsFromHost(url: Uri, query: String) = historyStorage
        .getSuggestions(query, maxNumberOfSuggestions * COMBINED_HISTORY_RESULTS_TO_FILTER_SCALE_FACTOR)
        .distinctBy { it.id }
        .sortedByDescending { it.score }
        .filter {
            it.url.toUri().sameHostWithoutMobileSubdomainAs(url)
        }
        .take(maxNumberOfSuggestions)
        .into(this@CombinedHistorySuggestionProvider, icons, loadUrlUseCase, showEditSuggestion)
}
