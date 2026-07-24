package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "contact_group_contact",
    primaryKeys = ["groupId", "contactLookupKey"],
    indices = [
        Index("groupId"),
        Index("contactLookupKey")
    ]
)
data class ContactGroupContactEntity(
    val groupId: Long = 0,
    val contactLookupKey: String = ""
)
