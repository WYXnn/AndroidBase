package com.wyx.commondatabase

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory

abstract class AppRoomDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context, dbName : String, passphrase: ByteArray): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    dbName
                )
                    .openHelperFactory(factory)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}