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
import dev.octoshrimpy.quik.data.db.dao.BlockedNumberDao
import dev.octoshrimpy.quik.model.BlockedNumber
import dev.octoshrimpy.quik.util.PhoneNumberUtils
import io.reactivex.Observable
import javax.inject.Inject

class BlockingRepositoryImpl @Inject constructor(
    private val blockedNumberDao: BlockedNumberDao,
    private val phoneNumberUtils: PhoneNumberUtils
) : BlockingRepository {

    override fun blockNumber(vararg addresses: String) {
        val blockedNumbers = blockedNumberDao.getAll().map { it.toModel() }
        val newAddresses = addresses.filter { address ->
            blockedNumbers.none { number -> phoneNumberUtils.compare(number.address, address) }
        }

        val maxId = blockedNumberDao.maxId() ?: 0L

        val newBlockedNumbers = newAddresses.mapIndexed { index, address ->
            BlockedNumber(maxId + 1 + index, address)
        }

        blockedNumberDao.insert(newBlockedNumbers.map { it.toEntity() })
    }

    override fun getBlockedNumbers(): Observable<List<BlockedNumber>> {
        return blockedNumberDao.getAllFlowable()
                .map { list -> list.map { it.toModel() } }
                .toObservable()
    }

    override fun getBlockedNumber(id: Long): BlockedNumber? {
        return blockedNumberDao.getById(id)?.toModel()
    }

    override fun isBlocked(address: String): Boolean {
        return blockedNumberDao.getAll()
                .any { number -> phoneNumberUtils.compare(number.address, address) }
    }

    override fun unblockNumber(id: Long) {
        blockedNumberDao.deleteByIds(listOf(id))
    }

    override fun unblockNumbers(vararg addresses: String) {
        val ids = blockedNumberDao.getAll()
                .filter { number ->
                    addresses.any { address -> phoneNumberUtils.compare(number.address, address) }
                }
                .map { number -> number.id }

        blockedNumberDao.deleteByIds(ids)
    }

}
