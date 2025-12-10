package com.posthog.hogotchi

import androidx.annotation.DrawableRes

enum class HogMood {
    HAPPY,
    HUNGRY,
    SLEEPY,
    PLAYFUL,
    CRITICAL,
    IDLE
}

data class HogState(
    val name: String = "Max",
    val happiness: Int = 70,
    val hunger: Int = 50,
    val energy: Int = 80,
    val mood: HogMood = HogMood.IDLE,
    val level: Int = 1,
    val totalInteractions: Int = 0
) {
    @get:DrawableRes
    val hogImage: Int
        get() = when (mood) {
            HogMood.HAPPY -> R.drawable.hog_happy
            HogMood.HUNGRY -> R.drawable.hog_hungry
            HogMood.SLEEPY -> R.drawable.hog_sleeping
            HogMood.PLAYFUL -> R.drawable.hog_playful
            HogMood.CRITICAL -> R.drawable.hog_critical
            HogMood.IDLE -> R.drawable.hog_idle
        }

    fun calculateMood(): HogMood {
        return when {
            hunger < 20 || happiness < 20 -> HogMood.CRITICAL
            hunger < 40 -> HogMood.HUNGRY
            energy < 30 -> HogMood.SLEEPY
            happiness > 80 && energy > 60 -> HogMood.PLAYFUL
            happiness > 60 -> HogMood.HAPPY
            else -> HogMood.IDLE
        }
    }
}

sealed class HogAction {
    data object Feed : HogAction()
    data object Play : HogAction()
    data object Sleep : HogAction()
    data object Pet : HogAction()
}
