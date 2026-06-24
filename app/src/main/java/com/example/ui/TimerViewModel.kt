package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.StudyRepository
import com.example.data.StudySession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimerMode(val durationSeconds: Int) {
    STUDY(50 * 60),
    BREAK(10 * 60)
}

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudyRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudyRepository(database.studyDao())
    }

    val totalStudyMinutes: StateFlow<Int?> = repository.totalStudyMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allSessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mode = MutableStateFlow(TimerMode.STUDY)
    val mode: StateFlow<TimerMode> = _mode.asStateFlow()

    private val _timeLeft = MutableStateFlow(TimerMode.STUDY.durationSeconds)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _sessionGoal = MutableStateFlow("")
    val sessionGoal: StateFlow<String> = _sessionGoal.asStateFlow()

    private val _selectedAudio = MutableStateFlow<String?>("Lofi Beats - Study Girl")
    val selectedAudio: StateFlow<String?> = _selectedAudio.asStateFlow()

    private var timerJob: Job? = null

    fun setGoal(goal: String) {
        _sessionGoal.value = goal
    }
    
    fun selectAudio(audio: String) {
        _selectedAudio.value = audio
    }

    fun toggleTimer() {
        if (_isRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                delay(1000L)
                if (_timeLeft.value > 0) {
                    _timeLeft.value -= 1
                }
                if (_timeLeft.value <= 0) {
                    completeSession()
                }
            }
        }
    }

    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timeLeft.value = _mode.value.durationSeconds
    }

    private suspend fun completeSession() {
        val durationMinutes = _mode.value.durationSeconds / 60
        repository.insert(StudySession(durationMinutes = durationMinutes, type = _mode.value.name))
        
        _mode.value = if (_mode.value == TimerMode.STUDY) TimerMode.BREAK else TimerMode.STUDY
        _timeLeft.value = _mode.value.durationSeconds
    }

    fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        
        val days = sessions.filter { it.type == TimerMode.STUDY.name }.map { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet().sortedDescending()
        
        if (days.isEmpty()) return 0
        
        var streak = 0
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val ONE_DAY = 24 * 60 * 60 * 1000L
        
        var currentDayToCheck = today
        if (days.contains(today)) {
            streak = 1
            currentDayToCheck = today - ONE_DAY
        } else if (days.contains(today - ONE_DAY)) {
            streak = 1
            currentDayToCheck = today - 2 * ONE_DAY
        } else {
            return 0
        }
        
        for (day in days) {
            if (day >= today - ONE_DAY) continue 
            if (day == currentDayToCheck) {
                streak++
                currentDayToCheck -= ONE_DAY
            } else {
                break
            }
        }
        
        return streak
    }
}
