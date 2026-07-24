package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.octoshrimpy.quik.data.db.entity.ConversationEntity
import dev.octoshrimpy.quik.data.db.entity.ConversationRecipientEntity
import dev.octoshrimpy.quik.data.db.entity.RecipientEntity
import dev.octoshrimpy.quik.data.db.relation.ConversationWithRelations
import dev.octoshrimpy.quik.data.db.relation.RecipientWithContact
import io.reactivex.Flowable

@Dao
interface ConversationDao {

    // Base inbox/archived list. unreadAtTop prepends an unread-first ordering.
    @Transaction
    @Query(
        "SELECT c.* FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.id != 0 AND c.archived = :archived AND c.blocked = 0 " +
            "AND EXISTS (SELECT 1 FROM conversation_recipient cr WHERE cr.conversationId = c.id) " +
            "AND (c.lastMessageId IS NOT NULL OR c.draft != '') " +
            "ORDER BY (CASE WHEN :unreadAtTop THEN m.read ELSE 0 END) ASC, " +
            "c.pinned DESC, (c.draft != '') DESC, m.date DESC"
    )
    fun getConversationsFlowable(archived: Boolean, unreadAtTop: Boolean): Flowable<List<ConversationWithRelations>>

    @Transaction
    @Query(
        "SELECT c.* FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.id != 0 AND c.archived = :archived AND c.blocked = 0 " +
            "AND EXISTS (SELECT 1 FROM conversation_recipient cr WHERE cr.conversationId = c.id) " +
            "AND (c.lastMessageId IS NOT NULL OR c.draft != '') " +
            "ORDER BY (CASE WHEN :unreadAtTop THEN m.read ELSE 0 END) ASC, " +
            "c.pinned DESC, (c.draft != '') DESC, m.date DESC"
    )
    fun getConversations(archived: Boolean, unreadAtTop: Boolean): List<ConversationWithRelations>

    @Transaction
    @Query(
        "SELECT c.* FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.id != 0 AND c.lastMessageId IS NOT NULL AND c.archived = 0 AND c.blocked = 0 " +
            "AND EXISTS (SELECT 1 FROM conversation_recipient cr WHERE cr.conversationId = c.id) " +
            "AND (c.pinned = 1 OR m.date > :threshold)"
    )
    fun getTopConversations(threshold: Long): List<ConversationWithRelations>

    @Transaction
    @Query(
        "SELECT c.* FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.id != 0 AND c.lastMessageId IS NOT NULL AND c.blocked = 0 " +
            "AND EXISTS (SELECT 1 FROM conversation_recipient cr WHERE cr.conversationId = c.id) " +
            "ORDER BY c.pinned DESC, m.date DESC"
    )
    fun getConversationsForSearch(): List<ConversationWithRelations>

    @Transaction
    @Query(
        "SELECT c.* FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.blocked = 1 ORDER BY m.date DESC"
    )
    fun getBlockedConversations(): List<ConversationWithRelations>

    @Transaction
    @Query(
        "SELECT c.* FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.blocked = 1 ORDER BY m.date DESC"
    )
    fun getBlockedConversationsFlowable(): Flowable<List<ConversationWithRelations>>

    @Transaction
    @Query("SELECT * FROM conversation WHERE id = :threadId LIMIT 1")
    fun getConversation(threadId: Long): ConversationWithRelations?

    @Transaction
    @Query("SELECT * FROM conversation WHERE id = :threadId LIMIT 1")
    fun getConversationFlowable(threadId: Long): Flowable<ConversationWithRelations>

    @Transaction
    @Query("SELECT * FROM conversation WHERE id IN (:threadIds)")
    fun getConversations(threadIds: Collection<Long>): List<ConversationWithRelations>

    @Transaction
    @Query("SELECT * FROM conversation")
    fun getAllConversations(): List<ConversationWithRelations>

    @Transaction
    @Query(
        "SELECT c.* FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.id != 0 AND c.lastMessageId IS NOT NULL AND c.archived = 0 AND c.blocked = 0 " +
            "AND EXISTS (SELECT 1 FROM conversation_recipient cr WHERE cr.conversationId = c.id) " +
            "ORDER BY m.date DESC LIMIT 5"
    )
    fun getUnmanagedConversationsFlowable(): Flowable<List<ConversationWithRelations>>

    @Query(
        "SELECT c.id FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.id != 0 AND c.archived = :archived AND c.blocked = 0 AND m.seen = 0 ORDER BY m.date DESC"
    )
    fun getUnseenIds(archived: Boolean): List<Long>

    @Query(
        "SELECT c.id FROM conversation c LEFT JOIN message m ON c.lastMessageId = m.id " +
            "WHERE c.id != 0 AND c.archived = :archived AND c.blocked = 0 AND m.read = 0 ORDER BY m.date DESC"
    )
    fun getUnreadIds(archived: Boolean): List<Long>

    // --- recipients ---

    @Transaction
    @Query("SELECT * FROM recipient")
    fun getRecipients(): List<RecipientWithContact>

    @Transaction
    @Query("SELECT * FROM recipient WHERE id = :id LIMIT 1")
    fun getRecipient(id: Long): RecipientWithContact?

    @Transaction
    @Query("SELECT * FROM recipient WHERE contactLookupKey IS NOT NULL")
    fun getUnmanagedRecipientsFlowable(): Flowable<List<RecipientWithContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipients(recipients: List<RecipientEntity>)

    @Query("UPDATE recipient SET contactLookupKey = :lookupKey WHERE id = :recipientId")
    fun setRecipientContact(recipientId: Long, lookupKey: String?)

    @Query("DELETE FROM recipient")
    fun deleteAllRecipients()

    // --- flag / field mutations ---

    @Query("UPDATE conversation SET name = :name WHERE id = :id")
    fun setName(id: Long, name: String)

    @Query("UPDATE conversation SET sendAsGroup = :sendAsGroup WHERE id = :id")
    fun setSendAsGroup(id: Long, sendAsGroup: Boolean)

    @Query("UPDATE conversation SET draft = :draft, draftDate = :draftDate WHERE id = :id")
    fun setDraft(id: Long, draft: String, draftDate: Long)

    @Query("UPDATE conversation SET lastMessageId = :messageId WHERE id = :id")
    fun setLastMessage(id: Long, messageId: Long?)

    @Query("UPDATE conversation SET archived = 1 WHERE id IN (:ids)")
    fun markArchived(ids: Collection<Long>)

    @Query("UPDATE conversation SET archived = 0 WHERE id IN (:ids)")
    fun markUnarchived(ids: Collection<Long>)

    @Query("UPDATE conversation SET pinned = 1 WHERE id IN (:ids)")
    fun markPinned(ids: Collection<Long>)

    @Query("UPDATE conversation SET pinned = 0 WHERE id IN (:ids)")
    fun markUnpinned(ids: Collection<Long>)

    @Query("UPDATE conversation SET blocked = 1, blockingClient = :client, blockReason = :reason WHERE id IN (:ids) AND blocked = 0")
    fun markBlocked(ids: Collection<Long>, client: Int?, reason: String?)

    @Query("UPDATE conversation SET blocked = 0, blockingClient = NULL, blockReason = NULL WHERE id IN (:ids)")
    fun markUnblocked(ids: Collection<Long>)

    // --- inserts / deletes ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipientJunctions(junctions: List<ConversationRecipientEntity>)

    @Query("DELETE FROM conversation_recipient WHERE conversationId = :conversationId")
    fun clearRecipientJunctions(conversationId: Long)

    @Transaction
    fun upsertConversation(conversation: ConversationEntity, junctions: List<ConversationRecipientEntity>) {
        insertConversation(conversation)
        clearRecipientJunctions(conversation.id)
        if (junctions.isNotEmpty()) insertRecipientJunctions(junctions)
    }

    @Query("DELETE FROM conversation WHERE id IN (:ids)")
    fun deleteConversations(ids: Collection<Long>)

    @Query("DELETE FROM conversation_recipient WHERE conversationId IN (:ids)")
    fun deleteRecipientJunctionsFor(ids: Collection<Long>)

    @Query("DELETE FROM conversation")
    fun deleteAllConversations()

    @Query("DELETE FROM conversation_recipient")
    fun deleteAllRecipientJunctions()
}
