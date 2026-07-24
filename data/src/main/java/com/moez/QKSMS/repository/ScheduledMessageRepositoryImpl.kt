package dev.octoshrimpy.quik.repository

import dev.octoshrimpy.quik.data.db.toEntity
import dev.octoshrimpy.quik.data.db.toModel
import dev.octoshrimpy.quik.data.db.dao.ScheduledMessageDao
import dev.octoshrimpy.quik.model.ScheduledMessage
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ScheduledMessageRepositoryImpl @Inject constructor(
    private val dao: ScheduledMessageDao
) : ScheduledMessageRepository {

    private val disposables = CompositeDisposable()

    override fun saveScheduledMessage(
        date: Long,
        subId: Int,
        recipients: List<String>,
        sendAsGroup: Boolean,
        body: String,
        attachments: List<String>,
        conversationId: Long
    ): ScheduledMessage {
        val id = (dao.maxId() ?: 0L) + 1

        val message = ScheduledMessage(id, date, subId, recipients.toMutableList(), sendAsGroup, body,
            attachments.toMutableList(), conversationId)

        dao.insert(message.toEntity())

        return message
    }

    override fun updateScheduledMessage(scheduledMessage: ScheduledMessage) {
        dao.insert(scheduledMessage.toEntity())
    }

    override fun getScheduledMessages(): Observable<List<ScheduledMessage>> {
        return dao.getAllFlowable()
            .map { list -> list.map { it.toModel() } }
            .toObservable()
    }

    override fun getScheduledMessage(id: Long): ScheduledMessage? {
        return dao.getById(id)?.toModel()
    }

    override fun getScheduledMessagesForConversation(conversationId: Long): Observable<List<ScheduledMessage>> {
        return dao.getForConversationFlowable(conversationId)
            .map { list -> list.map { it.toModel() } }
            .toObservable()
    }

    override fun deleteScheduledMessage(id: Long) {
        val subscription = Completable.fromAction {
            dao.deleteById(id)
        }.subscribeOn(Schedulers.io()) // Run on a background thread and switch to main if needed
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.v("Successfully deleted scheduled messages.")
            }, {
                Timber.e("Deleting scheduled messages failed.")
            })

        disposables.add(subscription)
    }

    override fun deleteScheduledMessages(ids: List<Long>) {
        ids.forEach { deleteScheduledMessage(it) }
    }

    override fun getAllScheduledMessageIdsSnapshot(): List<Long> {
        return dao.getAllIds()
    }
}
