# Hogotchi

A PostHog-themed virtual pet Android app for demonstrating push notifications via Firebase Cloud Messaging (FCM).

## What is this?

Hogotchi is a Tamagotchi-style virtual pet game featuring Max the Hedgehog (PostHog's mascot). It's designed as a fun, interactive demo for testing push notifications through PostHog Workflows.

**Features:**
- Care for your virtual hedgehog by feeding, playing, and letting it sleep
- Watch your hog's mood change based on its stats
- Receive push notifications reminding you to care for your hog
- Level up your hog through interactions
- Tap your hog to pet it!

## Setup

### Prerequisites

- Android Studio (latest version recommended)
- A Firebase project with FCM enabled
- Your own `google-services.json` file

### Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/PostHog/hogotchi.git
   cd hogotchi
   ```

2. Add your Firebase configuration:
   - Go to the [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use an existing one
   - Add an Android app with package name `com.posthog.hogotchi`
   - Download `google-services.json` and place it in `app/`

3. Open in Android Studio and run on a device/emulator

### Getting the FCM Token

When the app launches, the FCM registration token is logged to Logcat. Filter by `HogotchiMain` to find it:

```
D/HogotchiMain: FCM Token: <your-token-here>
```

## Sending Push Notifications

### Notification Payload Format

Hogotchi responds to both notification and data payloads:

```json
{
  "message": {
    "token": "<FCM_TOKEN>",
    "notification": {
      "title": "Max is hungry!",
      "body": "Feed your hog before it's too late!"
    },
    "data": {
      "action": "feed"
    }
  }
}
```

### Supported Actions

| Action | Effect |
|--------|--------|
| `feed` | Increases hunger stat, slight happiness boost |
| `play` | Increases happiness, decreases energy |
| `sleep` | Increases energy, slight hunger decrease |

### Example Notifications

**Hungry hog:**
```json
{
  "notification": {"title": "Max is hungry!", "body": "Your hog needs food!"},
  "data": {"action": "feed"}
}
```

**Playful hog:**
```json
{
  "notification": {"title": "Playtime?", "body": "Max wants to play with you!"},
  "data": {"action": "play"}
}
```

**Sleepy hog:**
```json
{
  "notification": {"title": "Sleepy hog", "body": "Max is getting tired..."},
  "data": {"action": "sleep"}
}
```

## Testing with PostHog Workflows

This app is designed to work with PostHog's Firebase push notification integration:

1. Set up a Firebase integration in PostHog
2. Create a workflow that sends push notifications
3. Use the FCM token from this app as the target
4. Watch Max react to your workflow triggers!

## Hog Moods

Max's appearance changes based on his stats:

| Mood | Condition | Image |
|------|-----------|-------|
| Critical | Hunger < 20% or Happiness < 20% | `hog_critical` |
| Hungry | Hunger < 40% | `hog_hungry` |
| Sleepy | Energy < 30% | `hog_sleeping` |
| Playful | Happiness > 80% and Energy > 60% | `hog_playful` |
| Happy | Happiness > 60% | `hog_happy` |
| Idle | Default state | `hog_idle` |

## Project Structure

```
app/src/main/java/com/posthog/hogotchi/
├── MainActivity.kt              # Main UI with Jetpack Compose
├── HogState.kt                  # Data model for hog state
├── HogViewModel.kt              # ViewModel with game logic
├── HogotchiMessagingService.kt  # FCM message handler
└── ui/theme/Theme.kt            # PostHog brand colors
```

## Assets

Hedgehog artwork is from PostHog's design system. See the main [PostHog repository](https://github.com/PostHog/posthog) for the full asset library.

## License

MIT License - feel free to use this as a reference for your own push notification demos!
