package org.artym_sysa.nihongo.room.Dao

import android.arch.persistence.room.*
import org.artym_sysa.nihongo.room.entity.Word


@Dao
interface WordDao {

    @Query("SELECT * FROM words")
    fun getAll(): List<Word>

    @Query("SELECT * FROM words WHERE id = :arg0")
    fun getById(id: Long): Word

    @Query("SELECT * FROM words WHERE group_id = :arg0")
    fun getByGroupId(id: Long): List<Word>

    @Query("SELECT * FROM words WHERE group_id = :arg0 AND status= :arg1")
    fun getByGroupIdAndFilterByStatus(id: Long, status: Long): List<Word>

    @Query("SELECT COUNT(*) FROM words WHERE group_id = :arg0 AND status= :arg1")
    fun getQuantityByGroupIdAndFilterByStatus(id: Long, status: Long): Long

    @Query("SELECT COUNT(*) FROM words WHERE group_id = :arg0")
    fun getQuantityByGroupId(id: Long): Long

    @Insert
    fun insert(word: Word): Long

    @Insert
    fun insert(words: List<Word>)

    @Delete
    fun delete(word: Word)

    @Delete
    fun delete(words: List<Word>)

    @Update
    fun update(word: Word)

    @Update
    fun update(words: List<Word>)

}