package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.octoshrimpy.quik.data.db.entity.MessageEntity
import dev.octoshrimpy.quik.data.db.entity.MmsPartEntity
import dev.octoshrimpy.quik.data.db.relation.MessageWithRelations
import io.reactivex.Flowable

/** Simple projection for old-message counts grouped by thread. */
class ThreadCount(val threadId: Long, val count: Int)

@Dao
interface MessageDao {

    // --- thread feed (query = "" matches all) ---

    @Transaction
    @Query(
        "SELECT DISTINCT m.* FROM message m " +
            "LEFT JOIN mms_part p ON p.messageId = m.contentId " +
            "WHERE m.threadId = :threadId AND m.isEmojiReaction = 0 " +
            "AND (:query = '' OR m.body LIKE '%' || :query || '%' OR p.text LIKE '%' || :query || '%') " +
            "ORDER BY m.date"
    )
    fun getMessagesFlowable(threadId: Long, query: String): Flowable<List<MessageWithRelations>>

    @Transaction
    @Query(
        "SELECT DISTINCT m.* FROM message m " +
            "LEFT JOIN mms_part p ON p.messageId = m.contentId " +
            "WHERE m.threadId = :threadId AND m.isEmojiReaction = 0 " +
            "AND (:query = '' OR m.body LIKE '%' || :query || '%' OR p.text LIKE '%' || :query || '%') " +
            "ORDER BY m.date"
    )
    fun getMessages(threadId: Long, query: String): List<MessageWithRelations>

    @Transaction
    @Query("SELECT * FROM message WHERE id = :id LIMIT 1")
    fun getMessage(id: Long): MessageWithRelations?

    @Transaction
    @Query("SELECT * FROM message WHERE id IN (:ids)")
    fun getMessages(ids: Collection<Long>): List<MessageWithRelations>

    @Transaction
    @Query("SELECT m.* FROM message m JOIN mms_part p ON p.messageId = m.contentId WHERE p.id = :partId LIMIT 1")
    fun getMessageForPart(partId: Long): MessageWithRelations?

    @Transaction
    @Query(
        "SELECT * FROM message WHERE threadId = :threadId AND boxId IN (0, 1) " +
            "AND type IN ('sms', 'mms') ORDER BY date DESC"
    )
    fun getLastIncomingMessages(threadId: Long): List<MessageWithRelations>

    @Transaction
    @Query("SELECT * FROM message WHERE seen = 0 AND read = 0 AND threadId = :threadId ORDER BY date")
    fun getUnreadUnseenMessages(threadId: Long): List<MessageWithRelations>

    @Transaction
    @Query("SELECT * FROM message WHERE read = 0 AND threadId = :threadId ORDER BY date")
    fun getUnreadMessages(threadId: Long): List<MessageWithRelations>

    @Query(
        "SELECT COUNT(*) FROM conversation c JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.archived = 0 AND c.blocked = 0 AND m.read = 0"
    )
    fun getUnreadCount(): Long

    @Transaction
    @Query(
        "SELECT DISTINCT m.* FROM message m " +
            "LEFT JOIN mms_part p ON p.messageId = m.contentId " +
            "WHERE m.body LIKE '%' || :query || '%' OR p.text LIKE '%' || :query || '%'"
    )
    fun searchMessages(query: String): List<MessageWithRelations>

    @Transaction
    @Query("SELECT * FROM message ORDER BY id")
    fun getAllForDedup(): List<MessageWithRelations>

    @Transaction
    @Query("SELECT * FROM message ORDER BY date")
    fun getAllMessagesByDate(): List<MessageWithRelations>

    @Transaction
    @Query("SELECT * FROM message WHERE threadId = :threadId ORDER BY date DESC")
    fun getMessagesByThreadDesc(threadId: Long): List<MessageWithRelations>

    @Transaction
    @Query(
        "SELECT DISTINCT m.* FROM message m " +
            "LEFT JOIN mms_part p ON p.messageId = m.contentId " +
            "WHERE (m.type = 'sms' AND m.body != '') " +
            "OR (m.type = 'mms' AND m.messageType != 130 AND p.text IS NOT NULL AND p.text != '') " +
            "ORDER BY m.date ASC"
    )
    fun getEmojiReactionCandidates(): List<MessageWithRelations>

    @Query("SELECT * FROM mms_part WHERE id = :id LIMIT 1")
    fun getPart(id: Long): MmsPartEntity?

    @Query(
        "SELECT p.* FROM mms_part p JOIN message m ON m.contentId = p.messageId " +
            "WHERE m.threadId = :threadId AND (p.type LIKE 'image/%' OR p.type LIKE 'video/%') ORDER BY p.id DESC"
    )
    fun getPartsForConversationFlowable(threadId: Long): Flowable<List<MmsPartEntity>>

    @Query("SELECT threadId, COUNT(*) AS count FROM message WHERE date < :threshold GROUP BY threadId")
    fun getOldMessageCounts(threshold: Long): List<ThreadCount>

    @Query("SELECT MAX(id) FROM message")
    fun maxId(): Long?

    @Query(
        "SELECT id FROM message WHERE (type = :type AND contentId = :contentId) " +
            "OR (id = :messageId AND contentId = 0) LIMIT 1"
    )
    fun getExistingMessageId(type: String, contentId: Long, messageId: Long): Long?

    // --- mark / update ---

    @Query("SELECT DISTINCT threadId FROM message WHERE seen = 0")
    fun getUnseenThreadIds(): List<Long>

    @Query("UPDATE message SET seen = 1 WHERE seen = 0")
    fun markAllSeen()

    @Query("UPDATE message SET seen = 1 WHERE threadId IN (:threadIds) AND seen = 0")
    fun markSeen(threadIds: Collection<Long>)

    @Query("UPDATE message SET seen = 1, read = 1 WHERE threadId IN (:threadIds) AND (read = 0 OR seen = 0)")
    fun markRead(threadIds: Collection<Long>)

    @Query("UPDATE message SET read = 0 WHERE id IN (SELECT lastMessageId FROM conversation WHERE id IN (:threadIds))")
    fun markUnread(threadIds: Collection<Long>)

    @Query("UPDATE message SET boxId = :boxId WHERE id = :id")
    fun updateBoxId(id: Long, boxId: Int)

    @Query("UPDATE message SET boxId = :boxId, errorCode = :errorCode WHERE id = :id")
    fun updateBoxIdAndError(id: Long, boxId: Int, errorCode: Int)

    @Query("UPDATE message SET deliveryStatus = :status, dateSent = :dateSent, read = 1 WHERE id = :id")
    fun updateDelivery(id: Long, status: Int, dateSent: Long)

    @Query("UPDATE message SET deliveryStatus = :status, dateSent = :dateSent, read = 1, errorCode = :errorCode WHERE id = :id")
    fun updateDeliveryFailed(id: Long, status: Int, dateSent: Long, errorCode: Int)

    @Query("UPDATE message SET date = :date WHERE id = :id")
    fun updateDate(id: Long, date: Long)

    @Query("UPDATE message SET isEmojiReaction = :value WHERE id = :id")
    fun setIsEmojiReaction(id: Long, value: Boolean)

    @Query("UPDATE message SET isEmojiReaction = 0")
    fun clearAllEmojiReactionFlags()

    @Query("SELECT boxId FROM message WHERE id = :id")
    fun getBoxId(id: Long): Int?

    // --- inserts / deletes ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParts(parts: List<MmsPartEntity>)

    @Query("DELETE FROM mms_part WHERE messageId = :contentId")
    fun deletePartsForContentId(contentId: Long)

    @Query("DELETE FROM mms_part WHERE messageId IN (:contentIds)")
    fun deletePartsForContentIds(contentIds: Collection<Long>)

    @Query("DELETE FROM message WHERE id IN (:ids)")
    fun deleteMessagesByIds(ids: Collection<Long>)

    @Query("DELETE FROM message WHERE threadId IN (:threadIds)")
    fun deleteMessagesForThreads(threadIds: Collection<Long>)

    @Transaction
    @Query("SELECT * FROM message WHERE date < :threshold")
    fun getMessagesOlderThan(threshold: Long): List<MessageWithRelations>

    @Query("DELETE FROM message WHERE date < :threshold")
    fun deleteOldMessages(threshold: Long)

    @Transaction
    fun upsertMessage(message: MessageEntity, parts: List<MmsPartEntity>) {
        insertMessage(message)
        deletePartsForContentId(message.contentId)
        if (parts.isNotEmpty()) insertParts(parts)
    }

    @Query("DELETE FROM message")
    fun deleteAllMessages()

    @Query("DELETE FROM mms_part")
    fun deleteAllParts()
}
