package org.artym_sysa.nihongo.room.Dao

import android.arch.persistence.room.*
import org.artym_sysa.nihongo.room.entity.Group
import org.artym_sysa.nihongo.room.entity.GroupNameAndId
import org.artym_sysa.nihongo.room.entity.GroupPojo

@Dao
interface GroupDao {
    @Query("SELECT g.*, (SELECT COUNT(w.id) FROM words w WHERE w.group_id=g.id) as wordsCount FROM groups g")
    fun getAll(): List<GroupPojo>

    @Query("SELECT * FROM groups WHERE id = :arg0")
    fun getById(id: Long?): Group

    @Query("SELECT COUNT(*) FROM groups WHERE name LIKE :arg0")
    fun getRowsCountByName(name: String): Long

    @Query("SELECT id, name FROM groups")
    fun getGroupsIdAndName(): List<GroupNameAndId>

    @Update
    fun update(group: Group)

    @Update
    fun update(groups: List<Group>)

    @Insert
    fun insert(group: Group): Long

    @Insert
    fun insert(groups: List<Group>)

    @Delete
    fun delete(group: Group)

    @Delete
    fun delete(groups: List<Group>)
}