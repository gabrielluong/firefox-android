/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.support.base.android

import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.PX

/**
 * A representation of an Android Padding.
 *
 * @property left Padding start in PX.
 * @property top Padding top in PX.
 * @property right Padding end in PX.
 * @property bottom Padding end in PX.
 */
data class Padding(
    @Dimension(unit = PX) val left: Int,
    @Dimension(unit = PX) val top: Int,
    @Dimension(unit = PX) val right: Int,
    @Dimension(unit = PX) val bottom: Int,
)
