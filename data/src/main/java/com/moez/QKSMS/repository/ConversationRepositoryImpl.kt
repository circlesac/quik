/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.octoshrimpy.quik.repository

import android.content.ContentUris
import android.content.Context
import dev.octoshrimpy.quik.compat.TelephonyCompat
import dev.octoshrimpy.quik.data.db.dao.ContactDao
import dev.octoshrimpy.quik.data.db.dao.ConversationDao
import dev.octoshrimpy.quik.data.db.dao.MessageDao
import dev.octoshrimpy.quik.data.db.recipientJunctions
import dev.octoshrimpy.quik.data.db.toEntity
import dev.octoshrimpy.quik.data.db.toModel
import dev.octoshrimpy.quik.extensions.map
import dev.octoshrimpy.quik.filter.ConversationFilter
import dev.octoshrimpy.quik.mapper.CursorToConversation
import dev.octoshrimpy.quik.mapper.CursorToRecipient
import dev.octoshrimpy.quik.model.Conversation
import dev.octoshrimpy.quik.model.Recipient
import dev.octoshrimpy.quik.model.SearchResult
import dev.octoshrimpy.quik.util.PhoneNumberUtils
import dev.octoshrimpy.quik.util.tryOrNull
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val conversationFilter: ConversationFilter,
    private val cursorToConversation: CursorToConversation,
    private val cursorToRecipient: CursorToRecipient,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val contactDao: ContactDao
) : ConversationRepository {

    override fun getConversations(
        unreadAtTop: Boolean,
        archived: Boolean
    ): Observable<List<Conversation>> =
        conversationDao.getConversationsFlowable(archived, unreadAtTop)
            .map { list -> list.map { it.toModel() } }
            .toObservable()

    override fun getConversationsSnapshot(unreadAtTop: Boolean): List<Conversation> =
        conversationDao.getConversations(false, unreadAtTop).map { it.toModel() }

    override fun getTopConversations(): List<Conversation> {
        val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

        return conversationDao.getTopConversations(threshold)
            .map { it.toModel() }
            .sortedWith(compareByDescending<Conversation> { conversation ->
                conversation.pinned
            }
                .thenByDescending { conversation ->
                    messageDao.getMessagesByThreadDesc(conversation.id)
                        .count { it.message.date > threshold }
                }
            )
    }

    override fun setConversationName(id: Long, name: String): Completable =
        Completable.fromAction {
            conversationDao.setName(id, name)
        }.subscribeOn(Schedulers.io()) // Ensure the operation is performed on a background thread

    override fun searchConversations(query: CharSequence): List<SearchResult> {
        val searchQuery = query.toString()

        val conversations = conversationDao.getConversationsForSearch().map { it.toModel() }

        val messagesByConversation = messageDao.searchMessages(searchQuery)
            .map { it.toModel() }
            .groupBy { message -> message.threadId }
            .filter { (threadId, _) -> conversations.firstOrNull { it.id == threadId } != null }
            .map { (threadId, messages) -> Pair(conversations.first { it.id == threadId }, messages.size) }
            .map { (conversation, messages) -> SearchResult(searchQuery, conversation, messages) }
            .sortedByDescending { result -> result.messages }
            .toList()

        return conversations
            .filter { conversation -> conversationFilter.filter(conversation, searchQuery) }
            .map {
                    conversation -> SearchResult(searchQuery, conversation, 0)
            } + messagesByConversation
    }

    override fun getBlockedConversations(): List<Conversation> =
        conversationDao.getBlockedConversations().map { it.toModel() }

    override fun getBlockedConversationsAsync(): Observable<List<Conversation>> =
        conversationDao.getBlockedConversationsFlowable()
            .map { list -> list.map { it.toModel() } }
            .toObservable()

    override fun getConversationAsync(threadId: Long): Observable<Conversation> =
        conversationDao.getConversationFlowable(threadId)
            .map { it.toModel() }
            .toObservable()

    override fun getConversation(threadId: Long): Conversation? =
        conversationDao.getConversation(threadId)?.toModel()

    override fun updateSendAsGroup(threadId: Long, sendAsGroup: Boolean): Unit? =
        conversationDao.setSendAsGroup(threadId, sendAsGroup)

    override fun getUnseenIds(archived: Boolean): List<Long> =
        conversationDao.getUnseenIds(archived)

    override fun getUnreadIds(archived: Boolean): List<Long> =
        conversationDao.getUnreadIds(archived)

    override fun getConversationAndLastSenderContactName(threadId: Long): Pair<Conversation?, String?>? =
        conversationDao.getConversation(threadId)?.toModel()
            ?.let { conversation ->
                val conversationLastSmsSender: String? = conversation.recipients.find { recipient ->
                    phoneNumberUtils.compare(recipient.address, conversation.lastMessage!!.address)
                }?.contact?.name

                Pair(conversation, conversationLastSmsSender)
            }

    override fun getConversations(vararg threadIds: Long): List<Conversation> =
        conversationDao.getConversations(threadIds.toList()).map { it.toModel() }

    override fun getUnmanagedConversations(): Observable<List<Conversation>> =
        conversationDao.getUnmanagedConversationsFlowable()
            .map { list -> list.map { it.toModel() } }
            .toObservable()
            .subscribeOn(Schedulers.io())

    override fun getRecipients(): List<Recipient> =
        conversationDao.getRecipients().map { it.toModel() }

    override fun getUnmanagedRecipients(): Observable<List<Recipient>> =
        conversationDao.getUnmanagedRecipientsFlowable()
            .map { list -> list.map { it.toModel() } }
            .toObservable()
            .subscribeOn(Schedulers.io())

    override fun getRecipient(recipientId: Long): Recipient? =
        conversationDao.getRecipient(recipientId)?.toModel()

    override fun createConversation(threadId: Long, sendAsGroup: Boolean) =
        createConversationFromCp(threadId, sendAsGroup)


    override fun getConversation(recipients: Collection<String>): Conversation? =
        conversationDao.getAllConversations()
            .map { it.toModel() }
            .filter { conversation -> conversation.recipients.size == recipients.size }
            .find { conversation ->
                conversation.recipients.map { it.address }.all { recipientAddress ->
                    recipients.any { phoneNumberUtils.compare(it, recipientAddress) }
                }
            }

    override fun createConversation(addresses: Collection<String>, sendAsGroup: Boolean) =
        TelephonyCompat.getOrCreateThreadId(context, addresses.toSet())
            .takeIf { it != 0L }
            ?.let { providerThreadId ->
                createConversationFromCp(providerThreadId, sendAsGroup) ?:
                    createEmptyConversation(providerThreadId, addresses, sendAsGroup)
            }

    override fun getOrCreateConversation(threadId: Long, sendAsGroup: Boolean) =
        getConversation(threadId) ?: createConversation(threadId, sendAsGroup)

    override fun getOrCreateConversation(addresses: Collection<String>, sendAsGroup: Boolean) =
        getConversation(addresses) ?: createConversation(addresses, sendAsGroup)

    override fun saveDraft(threadId: Long, draft: String) {
        conversationDao.setDraft(threadId, draft, System.currentTimeMillis())
    }

    override fun updateConversations(threadIds: Collection<Long>) {
        threadIds.forEach { threadId ->
            val lastMessageId = messageDao.getMessagesByThreadDesc(threadId)
                .firstOrNull()?.message?.id
            conversationDao.setLastMessage(threadId, lastMessageId)
        }
    }

    override fun markArchived(vararg threadIds: Long) =
        conversationDao.markArchived(threadIds.toList())

    override fun markUnarchived(threadIds: Collection<Long>) =
        conversationDao.markUnarchived(threadIds)

    override fun markPinned(vararg threadIds: Long) =
        conversationDao.markPinned(threadIds.toList())

    override fun markUnpinned(vararg threadIds: Long) =
        conversationDao.markUnpinned(threadIds.toList())

    override fun markBlocked(threadIds: Collection<Long>, blockingClient: Int, blockReason: String?) =
        conversationDao.markBlocked(threadIds, blockingClient, blockReason)

    override fun markUnblocked(vararg threadIds: Long) =
        conversationDao.markUnblocked(threadIds.toList())

    override fun deleteConversations(vararg threadIds: Long) {
        val ids = threadIds.toList()

        conversationDao.deleteConversations(ids)
        conversationDao.deleteRecipientJunctionsFor(ids)
        messageDao.deleteMessagesForThreads(ids)

        threadIds.forEach {
            context.contentResolver.delete(
                ContentUris.withAppendedId(TelephonyCompat.THREADS_CONTENT_URI, it),
                null,
                null
            )
        }
    }

    /**
     * Returns a [Conversation] from the system SMS ContentProvider, based on the [threadId]
     *
     * It should be noted that even if we have a valid [threadId], that does not guarantee that
     * we can return a [Conversation]. On some devices, the ContentProvider won't return the
     * conversation unless it contains at least 1 message
     */
    private fun createConversationFromCp(threadId: Long, sendAsGroup: Boolean): Conversation? =
        tryOrNull(true) {
            cursorToConversation.getConversationsCursor()
                ?.map(cursorToConversation::map)
                ?.firstOrNull { conversation -> conversation.id == threadId }
                ?.also { conversation ->
                    val contacts = contactDao.getContacts().map { it.toModel() }

                    // match recipients from provider to recipients stored in the db
                    val matchedRecipients = conversation.recipients
                        .mapNotNull { recipient ->
                            // map the recipient cursor to a list of recipients
                            cursorToRecipient.getRecipientCursor(recipient.id)?.use { cursor ->
                                cursor.map { cursorToRecipient.map(it) }
                            }
                        }
                        .flatten()
                        .map { recipient ->
                            recipient.apply {
                                contact = contacts.firstOrNull { contact ->
                                    contact.numbers.any {
                                        phoneNumberUtils.compare(it.address, address)
                                    }
                                }
                            }
                        }

                    conversation.apply {
                        recipients.clear()
                        recipients.addAll(matchedRecipients)

                        this.sendAsGroup =
                            if (recipients.size <= 1) false
                            else sendAsGroup

                        lastMessage = messageDao.getMessagesByThreadDesc(threadId)
                            .firstOrNull()?.toModel()
                    }

                    conversationDao.insertRecipients(conversation.recipients.map { it.toEntity() })
                    conversationDao.upsertConversation(
                        conversation.toEntity(),
                        conversation.recipientJunctions()
                    )
                }
        }

    /**
     * In some cases [createConversationFromCp] will return null if there are no messages present in the convo.
     * In order to allow the conversation to be accessed
     * we need to create an empty conversation in the db to match the conversation created in the content provider.
     *
     * This is a bit of a hack, but is necessary on devices running HyperOS or variants.
     */
    private fun createEmptyConversation(threadId: Long, addresses: Collection<String>, sendAsGroup: Boolean): Conversation {
        val contacts = contactDao.getContacts().map { it.toModel() }
        val matchedRecipients = addresses.map { address ->
            Recipient().apply {
                this.address = address
                contact = contacts.firstOrNull { contact ->
                    contact.numbers.any {
                        phoneNumberUtils.compare(it.address, address)
                    }
                }
            }
        }
        val conversation = Conversation().apply {
            id = threadId
            recipients.clear()
            recipients.addAll(matchedRecipients)
            this.sendAsGroup =
                if (recipients.size <= 1) false
                else sendAsGroup
        }

        conversationDao.insertRecipients(conversation.recipients.map { it.toEntity() })
        conversationDao.upsertConversation(
            conversation.toEntity(),
            conversation.recipientJunctions()
        )

        return conversation
    }

}
