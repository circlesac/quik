package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.octoshrimpy.quik.data.db.entity.BlockedNumberEntity
import io.reactivex.Flowable

@Dao
interface BlockedNumberDao {

    @Query("SELECT * FROM blocked_number")
    fun getAll(): List<BlockedNumberEntity>

    @Query("SELECT * FROM blocked_number")
    fun getAllFlowable(): Flowable<List<BlockedNumberEntity>>

    @Query("SELECT * FROM blocked_number WHERE id = :id")
    fun getById(id: Long): BlockedNumberEntity?

    @Query("SELECT MAX(id) FROM blocked_number")
    fun maxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<BlockedNumberEntity>)

    @Query("DELETE FROM blocked_number WHERE id IN (:ids)")
    fun deleteByIds(ids: List<Long>)
}
