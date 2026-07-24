package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.octoshrimpy.quik.data.db.entity.EmojiReactionEntity

@Dao
interface EmojiReactionDao {

    @Query("SELECT * FROM emoji_reaction WHERE targetMessageId = :targetMessageId")
    fun getForTarget(targetMessageId: Long): List<EmojiReactionEntity>

    @Query("SELECT MAX(id) FROM emoji_reaction")
    fun maxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: EmojiReactionEntity)

    @Query("DELETE FROM emoji_reaction WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM emoji_reaction WHERE targetMessageId = :targetMessageId AND senderAddress = :senderAddress")
    fun deleteForTargetAndSender(targetMessageId: Long, senderAddress: String)

    @Query("DELETE FROM emoji_reaction")
    fun deleteAll()
}
