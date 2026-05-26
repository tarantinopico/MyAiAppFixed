package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ProviderModelEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
