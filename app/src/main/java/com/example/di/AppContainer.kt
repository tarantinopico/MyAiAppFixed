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
import com.example.network.GenericOpenAiApi
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
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7)
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

    val genericRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/") // Base URL ignored via @Url
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val genericApi: GenericOpenAiApi by lazy { genericRetrofit.create(GenericOpenAiApi::class.java) }

    val openAiClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "https://api.openai.com/") }
    val anthropicClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "https://api.anthropic.com/") } // Will need custom client later if strictly direct Anthropic
    val openRouterClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "https://openrouter.ai/api/") }
    val mistralClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "https://api.mistral.ai/") }
    val deepSeekClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "https://api.deepseek.com/") }
    val togetherClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "https://api.together.xyz/") }
    val ollamaClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "http://localhost:11434/", false) }
    val localClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "http://localhost:8080/") }
    val customClient by lazy { com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, "https://custom.api/") } // Dummy, chatRepo should dynamically create Custom Clients based on DB

    // Repositories
    val modelRepository by lazy { ModelRepository(database.modelDao()) }
    val conversationRepository by lazy { ConversationRepository(database.conversationDao(), database.messageDao()) }
    val settingsRepository by lazy { SettingsRepository(multiKeyManager, appPreferences) }
    val customProviderRepository by lazy { com.example.repository.CustomProviderRepository(database.customProviderDao()) }
    val promptPreferences by lazy { com.example.data.repository.PromptPreferences(applicationContext) }
    val chatRepository by lazy {
        ChatRepository(
            conversationRepository,
            apiKeyFailoverManager,
            customProviderRepository,
            moshi,
            genericApi,
            groqClient,
            geminiClient,
            cerebrasClient,
            openAiClient,
            anthropicClient,
            openRouterClient,
            mistralClient,
            deepSeekClient,
            togetherClient,
            ollamaClient,
            localClient
        )
    }

    private val duckDuckGoProvider by lazy { com.example.data.search.DuckDuckGoSearchProvider() }
    val webSearchManager by lazy { com.example.domain.search.WebSearchManager(duckDuckGoProvider) }
    val sessionRestoreManager by lazy { com.example.repository.SessionRestoreManager(applicationContext) }
    val appPreferences by lazy { com.example.data.repository.AppPreferences(applicationContext) }
    val themePreferences by lazy { com.example.data.repository.ThemePreferences(applicationContext) }
}
