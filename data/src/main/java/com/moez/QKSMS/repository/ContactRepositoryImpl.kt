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

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import dev.octoshrimpy.quik.data.db.dao.ContactDao
import dev.octoshrimpy.quik.data.db.toModel
import dev.octoshrimpy.quik.extensions.asFlowable
import dev.octoshrimpy.quik.extensions.mapNotNull
import dev.octoshrimpy.quik.model.Contact
import dev.octoshrimpy.quik.model.ContactGroup
import dev.octoshrimpy.quik.util.Preferences
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val contactDao: ContactDao
) : ContactRepository {

    override fun findContactUri(address: String): Single<Uri> {
        return Flowable.just(address)
                .map {
                    when {
                        address.contains('@') -> {
                            Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(address))
                        }

                        else -> {
                            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
                        }
                    }
                }
                .mapNotNull { uri -> context.contentResolver.query(uri, arrayOf(BaseColumns._ID), null, null, null) }
                .flatMap { cursor -> cursor.asFlowable() }
                .firstOrError()
                .map { cursor -> cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID)) }
                .map { id -> Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id) }
    }

    override fun getContacts(): Observable<List<Contact>> {
        return contactDao.getContactsFlowable()
                .map { it.map { entity -> entity.toModel() } }
                .toObservable()
    }

    override fun getUnmanagedContact(lookupKey: String): Contact? {
        return contactDao.getContact(lookupKey)?.toModel()
    }

    override fun getUnmanagedAllContacts(): List<Contact> {
        return contactDao.getContacts().map { it.toModel() }
    }

    override fun getUnmanagedContacts(starred: Boolean): Observable<List<Contact>> {
        val mobileOnly = prefs.mobileOnly.get()
        val mobileLabel by lazy { Phone.getTypeLabel(context.resources, Phone.TYPE_MOBILE, "Mobile").toString() }

        return contactDao.getContactsFlowable()
                .map { it.map { entity -> entity.toModel() } }
                .toObservable()
                .map { contacts ->
                    if (mobileOnly) {
                        contacts.filter { contact ->
                            contact.numbers.any { number -> number.type == mobileLabel }
                        }
                    } else {
                        contacts
                    }
                }
                .map { contacts ->
                    if (starred) {
                        contacts.filter { it.starred }
                    } else {
                        contacts
                    }
                }
                .map { contacts ->
                    if (mobileOnly) {
                        contacts.map { contact ->
                            val filteredNumbers = contact.numbers.filter { number -> number.type == mobileLabel }
                            contact.numbers.clear()
                            contact.numbers.addAll(filteredNumbers)
                            contact
                        }
                    } else {
                        contacts
                    }
                }
                .map { contacts ->
                    contacts.sortedWith { c1, c2 ->
                        val initial = c1.name.firstOrNull()
                        val other = c2.name.firstOrNull()
                        if (initial?.isLetter() == true && other?.isLetter() != true) {
                            -1
                        } else if (initial?.isLetter() != true && other?.isLetter() == true) {
                            1
                        } else {
                            c1.name.compareTo(c2.name, ignoreCase = true)
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getUnmanagedContactGroups(): Observable<List<ContactGroup>> {
        return contactDao.getContactGroupsFlowable()
                .map { it.map { entity -> entity.toModel() }.filter { group -> group.contacts.isNotEmpty() } }
                .toObservable()
    }

    override fun setDefaultPhoneNumber(lookupKey: String, phoneNumberId: Long) {
        contactDao.setDefaultPhoneNumber(lookupKey, phoneNumberId)
    }

    override fun isContact(address: String): Boolean {
        val uri : Uri
        if (address.contains('@')) {
            uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(address))
        } else {
            uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
        }
        return context.contentResolver.query(
            uri,
            arrayOf(BaseColumns._ID),
            null,
            null,
            null
        )?.use { cursor ->
            cursor.count > 0
        } ?: false
    }

}
