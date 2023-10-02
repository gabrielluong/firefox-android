/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.share

/**
 * Interface used for adapting recent apps database entities
 */
interface RecentApp {

    /**
     * The activityName of the recent app.
     */
    val activityName: String

    /**
     * The score of the recent app (calculated based on number of selections - decay).
     * Value used for sorting in descending order the recent apps (most recent first)
     */
    val score: Double
}
