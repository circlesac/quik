package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phone_number",
    indices = [
        Index("contactLookupKey")
    ]
)
data class PhoneNumberEntity(
    @PrimaryKey val id: Long = 0,
    val contactLookupKey: String = "",
    val accountType: String? = "",
    val address: String = "",
    val type: String = "",
    val isDefault: Boolean = false
)
