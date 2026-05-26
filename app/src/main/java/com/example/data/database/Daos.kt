package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {
    @Query("SELECT * FROM provider_models ORDER BY sortOrder ASC")
    fun getAllModels(): Flow<List<ProviderModelEntity>>

    @Query("SELECT * FROM provider_models WHERE providerType = :providerType ORDER BY sortOrder ASC")
    fun getModelsByProvider(providerType: String): Flow<List<ProviderModelEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ProviderModelEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<ProviderModelEntity>)

    @Delete
    suspend fun deleteModel(model: ProviderModelEntity)

    @Query("UPDATE provider_models SET isDefault = 0 WHERE providerType = :providerType")
    suspend fun clearDefaults(providerType: String)

    @Query("SELECT COUNT(*) FROM provider_models WHERE isSeeded = 1")
    suspend fun getSeededModelCount(): Int
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE archived = :archived ORDER BY pinned DESC, updatedAt DESC")
    fun getRecentConversations(archived: Boolean = false): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversation(id: Long): Flow<ConversationEntity?>
    
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationSync(id: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("SELECT * FROM conversations WHERE title LIKE '%' || :query || '%'")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    suspend fun getMessagesSync(conversationId: Long): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long
    
    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: Long): MessageEntity?

    @Query("SELECT modelIdUsed, SUM(tokenCount) as totalTokens FROM messages WHERE tokenCount IS NOT NULL AND modelIdUsed IS NOT NULL GROUP BY modelIdUsed")
    fun getTokenStatsByModel(): Flow<List<TokenStatsResult>>
}

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY addedAt ASC")
    fun getAllApiKeys(): Flow<List<ApiKeyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(apiKey: ApiKeyEntity)
    
    @Update
    suspend fun updateApiKey(apiKey: ApiKeyEntity)

    @Delete
    suspend fun deleteApiKey(apiKey: ApiKeyEntity)
}
