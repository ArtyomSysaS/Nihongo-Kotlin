package org.artym_sysa.nihongo.room.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "groups")
data class Group(
        @PrimaryKey(autoGenerate = true)
        @NotNull
        @ColumnInfo(name = "id")
        var id: Long = 0,

        @ColumnInfo(name = "name")
        var name: String = "",

        @ColumnInfo(name = "date")
        var date: String = ""
)