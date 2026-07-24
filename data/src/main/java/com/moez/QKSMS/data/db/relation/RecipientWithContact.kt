package dev.octoshrimpy.quik.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.octoshrimpy.quik.data.db.entity.ContactEntity
import dev.octoshrimpy.quik.data.db.entity.RecipientEntity

data class RecipientWithContact(
    @Embedded val recipient: RecipientEntity,

    @Relation(
        parentColumn = "contactLookupKey",
        entityColumn = "lookupKey",
        entity = ContactEntity::class
    )
    val contact: ContactWithNumbers? = null
)
