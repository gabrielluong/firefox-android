/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.service.nimbus.messaging

/**
 * A data class that holds a representation of GleanPlum message from Nimbus.
 *
 * @property id identifies a message as unique.
 * @property data Data information provided from Nimbus.
 * @property action A strings that represents which action should be performed
 * after a message is clicked.
 * @property style Indicates how a message should be styled.
 * @property triggers A list of strings corresponding to targeting expressions. The message
 * will be shown if all expressions `true`.
 * @property metadata Metadata that help to identify if a message should shown.
 */
data class Message(
    val id: String,
    val data: MessageData,
    val action: String,
    val style: StyleData,
    val triggers: List<String>,
    val metadata: Metadata,
) {
    val maxDisplayCount: Int
        get() = style.maxDisplayCount

    val priority: Int
        get() = style.priority

    val surface: MessageSurfaceId
        get() = data.surface

    val isExpired: Boolean
        get() = metadata.displayCount >= maxDisplayCount

    /**
     * A data class that holds metadata that help to identify if a message should shown.
     *
     * @property id identifies a message as unique.
     * @property displayCount Indicates how many times a message is displayed.
     * @property pressed Indicates if a message has been clicked.
     * @property dismissed Indicates if a message has been closed.
     * @property lastTimeShown A timestamp indicating when was the last time, the message was shown.
     * @property latestBootIdentifier A unique boot identifier for when the message was last displayed
     * (this may be a boot count or a boot id).
     */
    data class Metadata(
        val id: String,
        val displayCount: Int = 0,
        val pressed: Boolean = false,
        val dismissed: Boolean = false,
        val lastTimeShown: Long = 0L,
        val latestBootIdentifier: String? = null,
    )
}
