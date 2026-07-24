package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.octoshrimpy.quik.data.db.entity.ContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupEntity
import dev.octoshrimpy.quik.data.db.entity.PhoneNumberEntity
import dev.octoshrimpy.quik.data.db.relation.ContactGroupWithContacts
import dev.octoshrimpy.quik.data.db.relation.ContactWithNumbers
import io.reactivex.Flowable

@Dao
interface ContactDao {

    @Transaction
    @Query("SELECT * FROM contact ORDER BY name")
    fun getContacts(): List<ContactWithNumbers>

    @Transaction
    @Query("SELECT * FROM contact ORDER BY name")
    fun getContactsFlowable(): Flowable<List<ContactWithNumbers>>

    @Transaction
    @Query("SELECT * FROM contact WHERE lookupKey = :lookupKey LIMIT 1")
    fun getContact(lookupKey: String): ContactWithNumbers?

    @Transaction
    @Query("SELECT * FROM contact_group")
    fun getContactGroupsFlowable(): Flowable<List<ContactGroupWithContacts>>

    // --- writes ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(contacts: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoneNumbers(numbers: List<PhoneNumberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContactGroups(groups: List<ContactGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContactGroupJunctions(junctions: List<ContactGroupContactEntity>)

    @Query("SELECT id FROM phone_number WHERE isDefault = 1")
    fun getDefaultPhoneNumberIds(): List<Long>

    @Query("UPDATE phone_number SET isDefault = 0 WHERE contactLookupKey = :lookupKey")
    fun clearDefaultNumbers(lookupKey: String)

    @Query("UPDATE phone_number SET isDefault = 1 WHERE id = :phoneNumberId")
    fun setDefaultNumber(phoneNumberId: Long)

    @Transaction
    fun setDefaultPhoneNumber(lookupKey: String, phoneNumberId: Long) {
        clearDefaultNumbers(lookupKey)
        setDefaultNumber(phoneNumberId)
    }

    @Query("DELETE FROM contact")
    fun deleteAllContacts()

    @Query("DELETE FROM contact_group")
    fun deleteAllContactGroups()

    @Query("DELETE FROM phone_number")
    fun deleteAllPhoneNumbers()

    @Query("DELETE FROM contact_group_contact")
    fun deleteAllContactGroupJunctions()
}
