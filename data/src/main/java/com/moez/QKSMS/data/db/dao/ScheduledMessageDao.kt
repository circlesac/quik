package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.octoshrimpy.quik.data.db.entity.ScheduledMessageEntity
import io.reactivex.Flowable

@Dao
interface ScheduledMessageDao {

    @Query("SELECT * FROM scheduled_message ORDER BY date")
    fun getAll(): List<ScheduledMessageEntity>

    @Query("SELECT * FROM scheduled_message ORDER BY date")
    fun getAllFlowable(): Flowable<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_message WHERE conversationId = :conversationId")
    fun getForConversationFlowable(conversationId: Long): Flowable<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_message WHERE id = :id")
    fun getById(id: Long): ScheduledMessageEntity?

    @Query("SELECT id FROM scheduled_message ORDER BY date")
    fun getAllIds(): List<Long>

    @Query("SELECT MAX(id) FROM scheduled_message")
    fun maxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: ScheduledMessageEntity)

    @Query("DELETE FROM scheduled_message WHERE id = :id")
    fun deleteById(id: Long)
}
