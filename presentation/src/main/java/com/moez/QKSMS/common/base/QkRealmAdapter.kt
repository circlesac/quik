/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.octoshrimpy.quik.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.octoshrimpy.quik.common.util.extensions.setVisible
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber

/**
 * List-backed base adapter (formerly a Realm RealmRecyclerViewAdapter). Holds a plain
 * [List] of detached model objects; call [updateData] with a new list to refresh. Keeps the
 * former public surface: [updateData], nullable [getItem], selection, [highlight], [emptyView].
 */
abstract class QkRealmAdapter<T, VH : QkViewHolder> : RecyclerView.Adapter<VH>() {

    var data: List<T> = emptyList()
        private set

    /**
     * This view can be set, and the adapter will automatically control the visibility of this view
     * based on the data
     */
    var emptyView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(data.isEmpty())
        }

    val selectionChanges: Subject<List<Long>> = BehaviorSubject.create()

    private var selection = mutableListOf<Long>()

    /**
     * Mark this message as highlighted
     */
    var highlight: Long = -1L
        set(value) {
            if (field == value) return

            field = value
            notifyDataSetChanged()
        }

    /**
     * Toggles the selected state for a particular view
     *
     * If we are currently in selection mode (we have an active selection), then the state will
     * toggle. If we are not in selection mode, then we will only toggle if [force]
     */
    protected fun toggleSelection(id: Long, force: Boolean = true): Boolean {
        if (!force && selection.isEmpty()) return false

        when (selection.contains(id)) {
            true -> selection -= id
            false -> selection += id
        }

        selectionChanges.onNext(selection)

        return true
    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

    fun clearSelection() {
        selection.clear()
        selectionChanges.onNext(selection)
        notifyDataSetChanged()
    }

    fun toggleSelectAll() {
        val needToSelectAll = (selection.size != itemCount)

        selection.clear()

        if (needToSelectAll) {
            for (position in 0 until itemCount)
                selection += getItemId(position)
        }

        // fire a single change event now
        selectionChanges.onNext(selection)

        notifyDataSetChanged()
    }

    open fun getItem(index: Int): T? {
        if (index < 0 || index >= data.size) {
            Timber.w("Only indexes in 0 until ${data.size} are allowed. Input was: $index")
            return null
        }

        return data[index]
    }

    override fun getItemCount(): Int = data.size

    open fun updateData(data: List<T>?) {
        this.data = data ?: emptyList()
        emptyView?.setVisible(this.data.isEmpty())
        notifyDataSetChanged()
    }
}
