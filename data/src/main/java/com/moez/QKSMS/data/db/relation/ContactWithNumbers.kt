package dev.octoshrimpy.quik.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.octoshrimpy.quik.data.db.entity.ContactEntity
import dev.octoshrimpy.quik.data.db.entity.PhoneNumberEntity

data class ContactWithNumbers(
    @Embedded val contact: ContactEntity,

    @Relation(parentColumn = "lookupKey", entityColumn = "contactLookupKey")
    val numbers: List<PhoneNumberEntity> = emptyList()
)
