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
        MessageEntity::class,
        ApiKeyEntity::class,
        CustomProviderEntity::class,
        PresetEntity::class,
        SkillEntity::class,
        PlanStepEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun customProviderDao(): CustomProviderDao
    abstract fun presetDao(): PresetDao
    abstract fun skillDao(): SkillDao
    abstract fun planStepDao(): PlanStepDao

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
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `api_keys` (`id` TEXT NOT NULL, `provider` TEXT NOT NULL, `label` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `isPreferred` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, `lastUsedAt` INTEGER, `failureCount` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE messages ADD COLUMN systemEventJson TEXT")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE provider_models ADD COLUMN contextLength INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE provider_models ADD COLUMN isReasoning INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE provider_models ADD COLUMN isVision INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE provider_models ADD COLUMN supportsTools INTEGER NOT NULL DEFAULT 0")
                
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `custom_providers` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `baseUrl` TEXT NOT NULL, `apiKey` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `presets` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `providerType` TEXT NOT NULL, `modelId` TEXT NOT NULL, `systemPrompt` TEXT NOT NULL, `temperature` REAL NOT NULL, `sortOrder` INTEGER NOT NULL, `iconColorHex` TEXT, PRIMARY KEY(`id`))"
                )
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `skills` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `systemPrompt` TEXT NOT NULL, `preferredProvider` TEXT, `isCustom` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `allowedTools` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `plan_steps` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `conversationId` INTEGER NOT NULL, `stepIndex` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `status` TEXT NOT NULL, `resultText` TEXT, `createdAt` INTEGER NOT NULL)"
                )
            }
        }
    }
}
