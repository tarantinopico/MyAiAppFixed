package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.SkillDao
import com.example.data.database.SkillEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SkillsViewModel(private val skillDao: SkillDao) : ViewModel() {
    
    val skills: StateFlow<List<SkillEntity>> = skillDao.getAllSkills()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    init {
        viewModelScope.launch {
            // Seed a default skill if empty
            val current = skillDao.getAllSkills().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
            if (current.isEmpty()) {
                skillDao.insertSkill(
                    SkillEntity(
                        id = "skill_dev",
                        name = "Senior Android Developer",
                        description = "Expert in Kotlin, Jetpack Compose and modern Android architecture.",
                        systemPrompt = "You are a Senior Android Engineer. You focus on clean architecture, Jetpack Compose best practices, and elegant UI.",
                        preferredProvider = null,
                        isCustom = false,
                        sortOrder = 1,
                        allowedTools = "file_generator",
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
        
    fun saveSkill(skill: SkillEntity) {
        viewModelScope.launch {
            skillDao.insertSkill(skill)
        }
    }
    
    fun deleteSkill(id: String) {
        viewModelScope.launch {
            val s = skills.value.find { it.id == id }
            if (s != null) {
                skillDao.deleteSkill(s)
            }
        }
    }
}
