# MoodTune - Unified Android Application

A unified Android application that combines 4 separate modules into one cohesive mood-based music recommendation system.

## Features

### 🔐 Authentication
- Google Sign-In integration using Firebase Auth
- Login persistence for returning users
- Automatic user profile creation

### 😊 Mood Detection
- **Face Detection**: Capture photo and detect mood using facial expressions
- **Chatbot**: Text-based mood detection through conversation
- **Quick Mood Buttons**: 5 quick mood selection buttons (Happy, Sad, Angry, Neutral, Surprise)

### 🎵 Music Recommendation
- Mood-based song recommendations using Audius API
- Personalized playlists per mood
- YouTube-style audio player with ExoPlayer

### 📊 Mood Reports
- **Daily Report**: Calendar view with mood indicators
- **Weekly Report**: Pie chart showing weekly mood distribution
- **Monthly Report**: Bar chart showing monthly mood trends

### ⚙️ Settings
- Edit Profile
- Change Password
- Theme Settings
- Notification Settings
- About App
- Sign Out

## Project Structure

```
MT/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/moodtune/app/
│   │       │   ├── auth/          # Login Activity
│   │       │   ├── home/          # Home Page Activity
│   │       │   ├── face/          # Face Detection
│   │       │   ├── chatbot/       # Chatbot
│   │       │   ├── music/         # Music Activities & Services
│   │       │   ├── reports/       # Report Activities
│   │       │   ├── settings/      # Settings Activities
│   │       │   ├── database/      # Database Handler
│   │       │   └── models/        # Data Models
│   │       └── res/               # Resources
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── settings.gradle.kts
```

## Setup Instructions

1. **Firebase Configuration**
   - Add your `google-services.json` file to `app/` directory
   - Update `default_web_client_id` in `strings.xml` with your Firebase OAuth client ID

2. **Backend Servers**
   - Face Detection: Update `BACKEND_URL` in `FaceDetectionActivity.java` (default: `http://172.20.10.4:5000/analyze`)
   - Chatbot: Update `BASE_URL` in `ChatbotActivity.java` (default: `http://192.168.1.4:5000`)

3. **Build the Project**
   ```bash
   ./gradlew build
   ```

## Database Schema

### Firestore Collections

#### `/users/{userId}`
```json
{
  "uid": "user_firebase_uid",
  "name": "User Display Name",
  "email": "user@example.com",
  "currentMood": "happy",
  "isFirstTime": false,
  "createdAt": 1234567890
}
```

#### `/moods/{userId}`
```json
{
  "history": [
    {
      "mood": "happy",
      "date": "2025-01-15T10:30:00Z",
      "source": "face",
      "confidence": 0.85
    }
  ]
}
```

#### `/playlists/{playlistId}`
```json
{
  "playlistId": "mood_userId_timestamp",
  "userId": "user_firebase_uid",
  "mood": "happy",
  "songs": [...],
  "createdAt": 1234567890,
  "updatedAt": 1234567890
}
```

## Navigation Flow

1. **First-time User**: Login → Home Page
2. **Returning User**: Auto-login → Home Page
3. **Mood Detection**: Face/Chatbot/Quick Mood → Save to DB → Song Recommendation
4. **Reports**: Sidebar Menu → Daily/Weekly/Monthly Reports
5. **Settings**: Sidebar Menu → Settings → Edit Profile/Change Password/etc.

## Dependencies

- Firebase Auth & Firestore
- Google Sign-In
- ExoPlayer (Audio playback)
- MPAndroidChart (Charts)
- Material Calendar View
- OkHttp (Network requests)
- Gson (JSON parsing)

## Notes

- All mood entries are saved with source tracking (face, chatbot, quick_mood)
- User ID is automatically retrieved from Firebase Auth
- Login state persists using SharedPreferences
- All activities check for user authentication before proceeding

