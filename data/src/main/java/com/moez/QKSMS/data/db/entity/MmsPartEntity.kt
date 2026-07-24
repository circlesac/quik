package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mms_part",
    indices = [
        Index("messageId")
    ]
)
data class MmsPartEntity(
    @PrimaryKey val id: Long = 0,
    val messageId: Long = 0,
    val type: String = "",
    val seq: Int = -1,
    val name: String? = null,
    val text: String? = null
)
