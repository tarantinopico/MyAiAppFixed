package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.storage.SecureApiKeyStore
import com.example.network.CerebrasApi
import com.example.network.CerebrasClient
import com.example.network.GeminiApi
import com.example.network.GeminiClient
import com.example.network.GroqApi
import com.example.network.GroqClient
import com.example.repository.ChatRepository
import com.example.repository.ConversationRepository
import com.example.repository.ModelRepository
import com.example.repository.SettingsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val applicationContext: Context) {

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ai_model_aggregator.db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
        .build()
    }

    val moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    val groqRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val cerebrasRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.cerebras.ai/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val geminiRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val groqApi: GroqApi by lazy { groqRetrofit.create(GroqApi::class.java) }
    val cerebrasApi: CerebrasApi by lazy { cerebrasRetrofit.create(CerebrasApi::class.java) }
    val geminiApi: GeminiApi by lazy { geminiRetrofit.create(GeminiApi::class.java) }

    val secureApiKeyStore by lazy { SecureApiKeyStore(applicationContext) }
    
    val multiKeyManager by lazy { com.example.data.repository.MultiKeyManager(database.apiKeyDao(), secureApiKeyStore) }
    val apiKeyFailoverManager by lazy { com.example.network.ApiKeyFailoverManager(multiKeyManager) }

    val groqClient by lazy { GroqClient(groqApi, moshi) }
    val cerebrasClient by lazy { CerebrasClient(cerebrasApi, moshi) }
    val geminiClient by lazy { GeminiClient(geminiApi, moshi) }

    // Repositories
    val modelRepository by lazy { ModelRepository(database.modelDao()) }
    val conversationRepository by lazy { ConversationRepository(database.conversationDao(), database.messageDao()) }
    val settingsRepository by lazy { SettingsRepository(multiKeyManager) }
    val chatRepository by lazy {
        ChatRepository(
            conversationRepository,
            apiKeyFailoverManager,
            groqClient,
            geminiClient,
            cerebrasClient
        )
    }
}
