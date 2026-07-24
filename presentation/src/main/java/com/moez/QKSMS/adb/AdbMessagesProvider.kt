/*
 * Copyright (C) 2026
 *
 * This file is part of QUIK.
 *
 * QUIK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QUIK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QUIK.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.octoshrimpy.quik.adb

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder
import android.os.Process
import dev.octoshrimpy.quik.injection.appComponent
import dev.octoshrimpy.quik.repository.ConversationRepository
import dev.octoshrimpy.quik.repository.MessageRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * ContentProvider that exposes read / send / delete / mark over the SMS store to `adb shell content`
 * (and to on-device automation), reusing QUIK's own repositories. Because QUIK is the permanent
 * default SMS app, it holds full read+write rights on the system SMS provider at all times, so this
 * bridge needs no default-role juggling — unlike a standalone helper such as adbsms.
 *
 * Authority is `<applicationId>.adb` (e.g. `dev.octoshrimpy.quik.adb`, or `...debug.adb` for debug
 * builds). Examples:
 *
 *   # read (newest first; optional ?threadId= and ?limit=)
 *   adb shell content query  --uri content://dev.octoshrimpy.quik.adb/messages
 *   adb shell content query  --uri content://dev.octoshrimpy.quik.adb/messages/137
 *   adb shell content query  --uri content://dev.octoshrimpy.quik.adb/conversations
 *
 *   # send
 *   adb shell content insert --uri content://dev.octoshrimpy.quik.adb/messages \
 *       --bind address:s:+821012345678 --bind body:s:"hello from adb"
 *
 *   # delete (single id, or a set via --where "id IN (1,2,3)")
 *   adb shell content delete --uri content://dev.octoshrimpy.quik.adb/messages/137
 *   adb shell content delete --uri content://dev.octoshrimpy.quik.adb/messages --where "id IN (1,2,3)"
 *
 *   # mark read/unread, or archive/unarchive the message's thread (either or both binds)
 *   adb shell content update --uri content://dev.octoshrimpy.quik.adb/messages/137 --bind read:i:1
 *   adb shell content update --uri content://dev.octoshrimpy.quik.adb/messages/137 --bind archived:i:1
 *
 * The /messages query hides messages in archived conversations by default (an inbox/agenda view);
 * pass ?includeArchived=1 to see them all.
 *
 * Security: the provider is exported (the `shell` uid lives in a different process, so adb can only
 * reach it when exported), which means any installed app could otherwise call it with no SMS
 * permission of its own. To prevent that, every call is gated **fail-closed** by the caller's uid —
 * kernel-supplied via Binder, so it cannot be spoofed. Only two callers are allowed:
 *
 *   - QUIK itself (same uid).
 *   - The `shell` uid, so `adb` works (adb is already gated by USB debugging + an authorized host).
 *
 * Every other caller is rejected with SecurityException. This is an adb-only bridge by design — it
 * is deliberately not exposed to other apps; on-device automation belongs inside QUIK as a feature,
 * not behind an app-facing provider. There is no URL token either — a query-parameter token leaks
 * into logcat/dumpsys, and the uid gate is stronger and uniform across read/send/delete/mark.
 */
class AdbMessagesProvider : ContentProvider() {

    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var conversationRepo: ConversationRepository

    private var injected = false
    private lateinit var authority: String
    private val matcher by lazy {
        UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(authority, "messages", MESSAGES)
            addURI(authority, "messages/#", MESSAGE_ID)
            addURI(authority, "conversations", CONVERSATIONS)
        }
    }

    override fun onCreate(): Boolean {
        authority = "${context!!.packageName}.adb"
        return true
    }

    /** Dagger graph isn't ready at provider onCreate (before Application.onCreate); inject lazily. */
    private fun ensureReady() {
        if (!injected) {
            appComponent.inject(this)
            injected = true
        }
    }

    /**
     * Fail-closed caller gate: only QUIK itself and the shell uid (for adb, itself gated by USB
     * debugging) may call in. Uses Binder.getCallingUid(), which the kernel supplies and a caller
     * cannot forge. This is an adb-only bridge — it is deliberately not opened to other apps.
     */
    private fun enforceCaller() {
        val uid = Binder.getCallingUid()
        if (uid != Process.myUid() && uid != Process.SHELL_UID) {
            Timber.w("adb bridge: rejected caller uid=$uid")
            throw SecurityException("adb bridge: only QUIK and adb (shell) may call; caller uid=$uid")
        }
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor {
        ensureReady()
        enforceCaller()

        return when (matcher.match(uri)) {
            MESSAGES, MESSAGE_ID -> {
                // Hide messages in archived conversations by default (an agenda/inbox view);
                // pass ?includeArchived=1 to see them all. Room repos expose per-thread reads,
                // so resolve the visible thread set from the conversation repo (non-archived,
                // plus archived when requested) and filter/collect messages against it.
                val includeArchived = uri.getQueryParameter("includeArchived") == "1"

                val visibleThreadIds = (conversationRepo.getConversationsSnapshot(false) +
                    if (includeArchived)
                        conversationRepo.getConversations(unreadAtTop = false, archived = true)
                            .blockingFirst()
                    else emptyList())
                    .map { it.id }
                    .toSet()

                val messages = when {
                    matcher.match(uri) == MESSAGE_ID ->
                        listOfNotNull(messageRepo.getMessage(ContentUris.parseId(uri)))

                    else -> {
                        val threadFilter = uri.getQueryParameter("threadId")?.toLongOrNull()
                        val threadIds =
                            if (threadFilter != null) listOf(threadFilter)
                            else visibleThreadIds.toList()
                        threadIds.flatMap { messageRepo.getMessagesSync(it) }
                    }
                }
                    .filter { includeArchived || it.threadId in visibleThreadIds }
                    .sortedByDescending { it.date }

                val limit = uri.getQueryParameter("limit")?.toIntOrNull() ?: messages.size

                MatrixCursor(MESSAGE_COLUMNS).apply {
                    messages.take(limit).forEach { m ->
                        // getSummary() returns the SMS body, or for MMS the subject + part
                        // summaries (incl. image labels) — so MMS text is exposed too, not just
                        // the empty `body` column.
                        addRow(arrayOf(
                            m.id, m.id, m.threadId, m.contentId, m.address, m.getSummary(),
                            m.date, m.dateSent, if (m.read) 1 else 0, if (m.seen) 1 else 0,
                            m.type, m.boxId, m.subId, if (m.locked) 1 else 0
                        ))
                    }
                }
            }

            CONVERSATIONS -> {
                val results = conversationRepo.getConversationsSnapshot(false)
                    .sortedByDescending { it.date }

                MatrixCursor(CONVERSATION_COLUMNS).apply {
                    results.forEach { c ->
                        addRow(arrayOf(
                            c.id, c.id, c.getTitle(),
                            c.recipients.joinToString(", ") { it.address },
                            c.snippet ?: "", c.date, if (c.unread) 1 else 0,
                            if (c.pinned) 1 else 0, if (c.blocked) 1 else 0
                        ))
                    }
                }
            }

            else -> throw IllegalArgumentException("Unsupported uri: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        ensureReady()
        enforceCaller()

        if (matcher.match(uri) != MESSAGES)
            throw IllegalArgumentException("insert (send) only supported on /messages: $uri")

        val address = values?.getAsString("address")
            ?: throw IllegalArgumentException("missing 'address'")
        val body = values.getAsString("body") ?: ""
        val subId = values.getAsInteger("subId") ?: -1

        val sent = messageRepo.sendNewMessages(subId, listOf(address), body, listOf(), false, 0)
        Timber.v("adb bridge: sent ${sent.size} message(s) to $address")

        return sent.firstOrNull()
            ?.let { ContentUris.withAppendedId(Uri.parse("content://$authority/messages"), it.id) }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        ensureReady()
        enforceCaller()

        val ids = when (matcher.match(uri)) {
            MESSAGE_ID -> listOf(ContentUris.parseId(uri))
            MESSAGES -> parseIdsFromSelection(selection)
            else -> throw IllegalArgumentException("Unsupported delete uri: $uri")
        }
        if (ids.isEmpty()) return 0

        // Look up affected threads before deleting so we can refresh those conversations after.
        val threadIds = ids.mapNotNull { messageRepo.getUnmanagedMessage(it)?.threadId }.distinct()

        // deleteMessages removes from both the local store and the system SMS provider (contentResolver.delete).
        messageRepo.deleteMessages(ids)
        conversationRepo.updateConversations(threadIds)

        Timber.v("adb bridge: deleted ${ids.size} message(s) across ${threadIds.size} thread(s)")
        return ids.size
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int {
        ensureReady()
        enforceCaller()

        val read = values?.getAsInteger("read")
        val archived = values?.getAsInteger("archived")
        if (read == null && archived == null)
            throw IllegalArgumentException("update supports the 'read' and/or 'archived' flag")

        val ids = when (matcher.match(uri)) {
            MESSAGE_ID -> listOf(ContentUris.parseId(uri))
            MESSAGES -> parseIdsFromSelection(selection)
            else -> throw IllegalArgumentException("Unsupported update uri: $uri")
        }
        if (ids.isEmpty()) return 0

        // read/unread and archive/unarchive both operate per-thread, so resolve threads from the ids.
        val threadIds = ids.mapNotNull { messageRepo.getUnmanagedMessage(it)?.threadId }.distinct()

        if (read != null) {
            if (read != 0) messageRepo.markRead(threadIds) else messageRepo.markUnread(threadIds)
        }
        if (archived != null) {
            if (archived != 0) conversationRepo.markArchived(*threadIds.toLongArray())
            else conversationRepo.markUnarchived(threadIds)
        }
        conversationRepo.updateConversations(threadIds)
        return ids.size
    }

    override fun getType(uri: Uri): String? = when (matcher.match(uri)) {
        MESSAGES, CONVERSATIONS -> "vnd.android.cursor.dir/vnd.$authority.item"
        MESSAGE_ID -> "vnd.android.cursor.item/vnd.$authority.item"
        else -> null
    }

    /** Pull the numeric ids out of a `where` clause like `id IN (1, 2, 3)` or `id = 5`. */
    private fun parseIdsFromSelection(selection: String?): List<Long> =
        selection?.let { Regex("\\d+").findAll(it).map { m -> m.value.toLong() }.toList() } ?: emptyList()

    companion object {
        private const val MESSAGES = 1
        private const val MESSAGE_ID = 2
        private const val CONVERSATIONS = 3

        private val MESSAGE_COLUMNS = arrayOf(
            "_id", "id", "threadId", "contentId", "address", "body",
            "date", "dateSent", "read", "seen", "type", "boxId", "subId", "locked"
        )

        private val CONVERSATION_COLUMNS = arrayOf(
            "_id", "id", "title", "addresses", "snippet", "date", "unread", "pinned", "blocked"
        )
    }

}
