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
package dev.octoshrimpy.quik.repository

import dev.octoshrimpy.quik.data.db.toEntity
import dev.octoshrimpy.quik.data.db.toModel
import dev.octoshrimpy.quik.data.db.dao.MessageContentFilterDao
import dev.octoshrimpy.quik.model.MessageContentFilter
import dev.octoshrimpy.quik.model.MessageContentFilterData
import io.reactivex.Observable
import javax.inject.Inject

class MessageContentFilterRepositoryImpl @Inject constructor(
    private val messageContentFilterDao: MessageContentFilterDao
) : MessageContentFilterRepository {
    override fun createFilter(data: MessageContentFilterData) {
        val id = (messageContentFilterDao.maxId() ?: 0L) + 1
        val filter = MessageContentFilter(id, data.value, data.caseSensitive, data.isRegex, data.includeContacts)
        messageContentFilterDao.insert(filter.toEntity())
    }

    override fun getMessageContentFilters(): Observable<List<MessageContentFilter>> {
        return messageContentFilterDao.getAllFlowable()
            .map { list -> list.map { it.toModel() } }
            .toObservable()
    }

    override fun getMessageContentFilter(id: Long): MessageContentFilter? {
        return messageContentFilterDao.getById(id)?.toModel()
    }

    override fun isBlocked(messageBody: String, address: String, contactsRepo: ContactRepository): Boolean {
        val isContact = contactsRepo.isContact(address)

        return messageContentFilterDao.getAll()
            .map { it.toModel() }
            .any { filter ->
                if (isContact && !filter.includeContacts) {
                    false
                } else if (filter.isRegex) {
                    Regex(filter.value).matches(messageBody)
                } else if (filter.caseSensitive) {
                    val regexp = "[\\s\\S]*\\b" + Regex.escape(filter.value) + "\\b[\\s\\S]*"
                    Regex(regexp).matches(messageBody)
                } else {
                    val regexp = "[\\s\\S]*\\b" + Regex.escape(filter.value.lowercase()) + "\\b[\\s\\S]*"
                    Regex(regexp).matches(messageBody.lowercase())
                }
            }
    }

    override fun removeFilter(id: Long) {
        messageContentFilterDao.deleteById(id)
    }

}
