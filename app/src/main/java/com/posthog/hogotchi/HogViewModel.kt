package com.posthog.hogotchi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HogViewModel(application: Application) : AndroidViewModel(application) {

    private val _hogState = MutableStateFlow(HogState())
    val hogState: StateFlow<HogState> = _hogState.asStateFlow()

    private val _showSurprise = MutableStateFlow(false)
    val showSurprise: StateFlow<Boolean> = _showSurprise.asStateFlow()

    private val _lastAction = MutableStateFlow<String?>(null)
    val lastAction: StateFlow<String?> = _lastAction.asStateFlow()

    init {
        startStatDecay()
    }

    private fun startStatDecay() {
        viewModelScope.launch {
            while (true) {
                delay(30_000) // Every 30 seconds
                _hogState.update { state ->
                    val newHunger = (state.hunger - 2).coerceIn(0, 100)
                    val newEnergy = (state.energy - 1).coerceIn(0, 100)
                    val newHappiness = (state.happiness - 1).coerceIn(0, 100)
                    state.copy(
                        hunger = newHunger,
                        energy = newEnergy,
                        happiness = newHappiness
                    ).let { it.copy(mood = it.calculateMood()) }
                }
            }
        }
    }

    fun onAction(action: HogAction) {
        _hogState.update { state ->
            val newState = when (action) {
                is HogAction.Feed -> {
                    _lastAction.value = "${state.name} loves the snacks!"
                    state.copy(
                        hunger = (state.hunger + 30).coerceIn(0, 100),
                        happiness = (state.happiness + 5).coerceIn(0, 100),
                        totalInteractions = state.totalInteractions + 1
                    )
                }
                is HogAction.Play -> {
                    _lastAction.value = "${state.name} had fun playing!"
                    state.copy(
                        happiness = (state.happiness + 25).coerceIn(0, 100),
                        energy = (state.energy - 15).coerceIn(0, 100),
                        hunger = (state.hunger - 5).coerceIn(0, 100),
                        totalInteractions = state.totalInteractions + 1
                    )
                }
                is HogAction.Sleep -> {
                    _lastAction.value = "${state.name} is well rested!"
                    state.copy(
                        energy = (state.energy + 40).coerceIn(0, 100),
                        hunger = (state.hunger - 10).coerceIn(0, 100),
                        totalInteractions = state.totalInteractions + 1
                    )
                }
                is HogAction.Pet -> {
                    _lastAction.value = "${state.name} feels loved!"
                    state.copy(
                        happiness = (state.happiness + 10).coerceIn(0, 100),
                        totalInteractions = state.totalInteractions + 1
                    )
                }
            }
            val leveledUp = newState.totalInteractions / 10 > state.level - 1
            newState.copy(
                mood = newState.calculateMood(),
                level = if (leveledUp) state.level + 1 else state.level
            )
        }
    }

    fun onHogTapped() {
        viewModelScope.launch {
            _showSurprise.value = true
            delay(500)
            _showSurprise.value = false
        }
        onAction(HogAction.Pet)
    }

    fun handleNotificationAction(action: String) {
        when (action.lowercase()) {
            "feed" -> onAction(HogAction.Feed)
            "play" -> onAction(HogAction.Play)
            "sleep" -> onAction(HogAction.Sleep)
        }
    }
}
