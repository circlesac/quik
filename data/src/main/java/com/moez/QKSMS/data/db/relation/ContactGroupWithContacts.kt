package dev.octoshrimpy.quik.data.db.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.octoshrimpy.quik.data.db.entity.ContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupEntity

data class ContactGroupWithContacts(
    @Embedded val group: ContactGroupEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "lookupKey",
        associateBy = Junction(
            value = ContactGroupContactEntity::class,
            parentColumn = "groupId",
            entityColumn = "contactLookupKey"
        ),
        entity = ContactEntity::class
    )
    val contacts: List<ContactWithNumbers> = emptyList()
)
