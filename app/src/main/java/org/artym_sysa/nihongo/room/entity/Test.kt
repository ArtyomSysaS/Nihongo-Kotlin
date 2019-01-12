package org.artym_sysa.nihongo.room.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(
        tableName = "tests",
        foreignKeys = [(ForeignKey(
                entity = Group::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("group_id"),
                onDelete = ForeignKey.CASCADE
        ))]
)

data class Test(
        @PrimaryKey(autoGenerate = true)
        @NotNull
        @ColumnInfo(name = "id")
        var id: Long = 0,

        @NotNull
        @ColumnInfo(name = "date")
        var data: String = "",

        @NotNull
        @ColumnInfo(name = "group_id")
        var groupId: Long = 0,

        @NotNull
        @ColumnInfo(name = "correct")
        var correct: Int= 0,

        @NotNull
        @ColumnInfo(name = "incorrect")
        var incorrect: Int = 0,

        @NotNull
        @ColumnInfo(name = "history")
        var history: String = ""
)



