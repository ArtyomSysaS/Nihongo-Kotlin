package org.artym_sysa.nihongo.room.Dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.artym_sysa.nihongo.room.entity.Test

@Dao
interface TestDao {

    @Query("SELECT * FROM tests")
    fun getAll(): List<Test>

    @Query("SELECT * FROM tests WHERE group_id = :arg0")
    fun getByGroupId(id: Long?): List<Test>

    @Query("SELECT * FROM tests WHERE id = :arg0")
    fun getById(id: Long?): Test

    @Insert
    fun insert(test: Test): Long

    @Insert
    fun insert(tests: List<Test>)

    @Delete
    fun delete(test: Test)

    @Delete
    fun delete(tests: List<Test>)
}
