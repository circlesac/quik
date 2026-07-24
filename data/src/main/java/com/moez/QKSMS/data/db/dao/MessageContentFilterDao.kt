package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.octoshrimpy.quik.data.db.entity.MessageContentFilterEntity
import io.reactivex.Flowable

@Dao
interface MessageContentFilterDao {

    @Query("SELECT * FROM message_content_filter")
    fun getAll(): List<MessageContentFilterEntity>

    @Query("SELECT * FROM message_content_filter")
    fun getAllFlowable(): Flowable<List<MessageContentFilterEntity>>

    @Query("SELECT * FROM message_content_filter WHERE id = :id")
    fun getById(id: Long): MessageContentFilterEntity?

    @Query("SELECT MAX(id) FROM message_content_filter")
    fun maxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: MessageContentFilterEntity)

    @Query("DELETE FROM message_content_filter WHERE id = :id")
    fun deleteById(id: Long)
}
