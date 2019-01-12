package org.artym_sysa.nihongo.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import org.artym_sysa.nihongo.room.Dao.GroupDao
import org.artym_sysa.nihongo.room.Dao.TestDao
import org.artym_sysa.nihongo.room.Dao.WordDao
import org.artym_sysa.nihongo.room.entity.Group
import org.artym_sysa.nihongo.room.entity.Test
import org.artym_sysa.nihongo.room.entity.Word

@Database(
        entities = arrayOf(Group::class, Word::class, Test::class),
        version = 1,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "Nihongo")
                        .allowMainThreadQueries()
                        .build()
    }

    public
    abstract fun groupDao(): GroupDao

    abstract fun wordDao(): WordDao

    abstract fun testDao(): TestDao
}