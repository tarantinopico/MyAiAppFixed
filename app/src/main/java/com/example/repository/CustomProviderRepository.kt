package com.example.repository

import com.example.data.database.CustomProviderDao
import com.example.data.database.CustomProviderEntity
import kotlinx.coroutines.flow.Flow

class CustomProviderRepository(private val customProviderDao: CustomProviderDao) {
    fun getAllCustomProviders(): Flow<List<CustomProviderEntity>> = customProviderDao.getAllCustomProviders()
    
    suspend fun insertCustomProvider(provider: CustomProviderEntity) {
        customProviderDao.insertCustomProvider(provider)
    }

    suspend fun updateCustomProvider(provider: CustomProviderEntity) {
        customProviderDao.updateCustomProvider(provider)
    }

    suspend fun deleteCustomProvider(provider: CustomProviderEntity) {
        customProviderDao.deleteCustomProvider(provider)
    }
}
