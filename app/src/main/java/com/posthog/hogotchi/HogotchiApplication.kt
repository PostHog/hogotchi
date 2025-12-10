package com.posthog.hogotchi

import android.app.Application
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

class HogotchiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = PostHogAndroidConfig(
            apiKey = "phc_2NTTOtu0cozbsG1dovE4v4eFviuA93I0mFvCybWuDRP",
            host = "http://10.0.2.2:8010"
        ).apply {
            debug = true
            flushAt = 1  // Flush after every event
            flushIntervalSeconds = 5  // Also flush every 5 seconds
        }

        PostHogAndroid.setup(this, config)
    }
}
