package dev.octoshrimpy.quik.data.db.entity

import android.provider.Telephony.Sms
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message",
    indices = [
        Index("threadId"),
        Index("date")
    ]
)
data class MessageEntity(
    @PrimaryKey val id: Long = 0,
    val threadId: Long = 0,
    val contentId: Long = 0,
    val address: String = "",
    val boxId: Int = 0,
    val type: String = "",
    val date: Long = 0,
    val dateSent: Long = 0,
    val seen: Boolean = false,
    val read: Boolean = false,
    val locked: Boolean = false,
    val subId: Int = -1,
    val body: String = "",
    val errorCode: Int = 0,
    val deliveryStatus: Int = Sms.STATUS_NONE,
    val attachmentTypeString: String = "NOT_LOADED",
    val mmsDeliveryStatusString: String = "",
    val readReportString: String = "",
    val errorType: Int = 0,
    val messageSize: Int = 0,
    val messageType: Int = 0,
    val mmsStatus: Int = 0,
    val subject: String = "",
    val textContentType: String = "",
    val isEmojiReaction: Boolean = false,
    val sendAsGroup: Boolean = false
)
