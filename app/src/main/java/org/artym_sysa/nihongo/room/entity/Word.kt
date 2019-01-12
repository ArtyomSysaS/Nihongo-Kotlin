package org.artym_sysa.nihongo.room.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(
        tableName = "words",
        foreignKeys = [(ForeignKey(
                entity = Group::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("group_id"),
                onDelete = ForeignKey.CASCADE
        ))]
)
data class Word(
        @PrimaryKey(autoGenerate = true)
        @NotNull
        @ColumnInfo(name = "id")
        var id: Long = 0,

        @ColumnInfo(name = "group_id")
        var groupId: Long = 0,

        @ColumnInfo(name = "text")
        var text: String = "",

        @ColumnInfo(name = "meaning")
        var meaning: String = "",

        @ColumnInfo(name = "reading")
        var reading: String = "",

        @ColumnInfo(name = "extra_fields")
        var fields: String = "",

        @ColumnInfo(name = "status")
        var status: Int = 0
)