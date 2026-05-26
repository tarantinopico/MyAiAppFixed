package com.example.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GroqApi {
    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun createChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json", "Accept: text/event-stream")
    @Streaming
    suspend fun createChatCompletionStream(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): Response<ResponseBody>
}

interface CerebrasApi {
    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun createChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json", "Accept: text/event-stream")
    @Streaming
    suspend fun createChatCompletionStream(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): Response<ResponseBody>
}

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    @Headers("Content-Type: application/json")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>

    @POST("v1beta/models/{model}:streamGenerateContent?alt=sse")
    @Headers("Content-Type: application/json")
    @Streaming
    suspend fun streamGenerateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<ResponseBody>
}
