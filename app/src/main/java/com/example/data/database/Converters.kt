package com.example.data.database

import androidx.room.TypeConverter
import com.example.domain.model.ProviderType
import com.example.domain.model.MessageRole

class Converters {
    @TypeConverter
    fun fromProviderType(value: ProviderType): String = value.name

    @TypeConverter
    fun toProviderType(value: String): ProviderType = enumValueOf(value)

    @TypeConverter
    fun fromMessageRole(value: MessageRole): String = value.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = enumValueOf(value)
}
