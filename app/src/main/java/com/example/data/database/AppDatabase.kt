package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ProviderModelEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE messages ADD COLUMN generationTimeMs INTEGER")
                database.execSQL("ALTER TABLE messages ADD COLUMN tokenCount INTEGER")
                database.execSQL("ALTER TABLE messages ADD COLUMN modelIdUsed TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE provider_models SET modelId = 'llama-3.1-8b-instant', displayName = 'Llama 3.1 8B' WHERE modelId = 'llama3-8b-8192'")
                database.execSQL("UPDATE provider_models SET modelId = 'llama-3.3-70b-versatile', displayName = 'Llama 3.3 70B' WHERE modelId = 'llama3-70b-8192'")
                database.execSQL("UPDATE conversations SET selectedModelId = 'llama-3.1-8b-instant' WHERE selectedModelId = 'llama3-8b-8192'")
                database.execSQL("UPDATE conversations SET selectedModelId = 'llama-3.3-70b-versatile' WHERE selectedModelId = 'llama3-70b-8192'")
            }
        }
    }
}
