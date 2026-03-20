# MoodTune Integration Guide

## Step-by-Step Integration Instructions

### 1. Firebase Setup

1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package name: `com.moodtune.app`
3. Download `google-services.json` and place it in `app/` directory
4. Enable Authentication → Sign-in method → Google
5. Enable Firestore Database
6. Copy your OAuth 2.0 Client ID from Firebase Console → Project Settings → Your apps → SHA certificate fingerprints
7. Update `strings.xml` with your `default_web_client_id`

### 2. Backend Server Setup

#### Face Detection Server
- Update `BACKEND_URL` in `FaceDetectionActivity.java`
- Default: `http://172.20.10.4:5000/analyze`
- Server should accept POST requests with image data and return JSON:
  ```json
  {
    "emotion": "happy",
    "confidence": 0.85
  }
  ```

#### Chatbot Server
- Update `BASE_URL` in `ChatbotActivity.java`
- Default: `http://192.168.1.4:5000` (for real device) or `http://10.0.2.2:5000` (for emulator)
- Server should accept POST requests to `/predict` with JSON:
  ```json
  {
    "text": "user message"
  }
  ```
- Server should return:
  ```json
  {
    "mood": "happy",
    "confidence": "0.85"
  }
  ```

### 3. Build Configuration

1. Open project in Android Studio
2. Sync Gradle files
3. Ensure all dependencies are downloaded
4. Build the project: `Build > Make Project`

### 4. Testing Checklist

- [ ] Login with Google Sign-In works
- [ ] Home Page displays correctly
- [ ] Face Detection captures photo and detects mood
- [ ] Chatbot sends messages and receives mood
- [ ] Quick Mood buttons save mood and navigate to songs
- [ ] Song recommendation loads and plays
- [ ] Sidebar menu opens and navigates to reports
- [ ] Daily/Weekly/Monthly reports display correctly
- [ ] Settings page opens and functions work
- [ ] Sign out works correctly

### 5. Common Issues

#### Issue: Google Sign-In fails
- **Solution**: Check that `google-services.json` is in `app/` directory
- **Solution**: Verify `default_web_client_id` in `strings.xml` matches Firebase Console

#### Issue: Face Detection server error
- **Solution**: Check server is running and accessible
- **Solution**: Update `BACKEND_URL` to correct IP address
- **Solution**: Ensure server accepts POST requests with image data

#### Issue: Chatbot server error
- **Solution**: For emulator, use `10.0.2.2` instead of localhost
- **Solution**: For real device, use your computer's local IP address
- **Solution**: Ensure server is running and accessible

#### Issue: Reports show no data
- **Solution**: Ensure moods are being saved to Firestore
- **Solution**: Check Firestore rules allow read access
- **Solution**: Verify user ID is correct in report activities

### 6. Database Rules

Set Firestore rules to:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /moods/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /playlists/{playlistId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 7. Package Structure

All activities are organized by feature:
- `auth/` - Login
- `home/` - Home Page
- `face/` - Face Detection
- `chatbot/` - Chatbot
- `music/` - Music Activities & Services
- `reports/` - Report Activities
- `settings/` - Settings Activities
- `database/` - Database Handler
- `models/` - Data Models

### 8. Key Features Implemented

✅ Unified login system (from AudiusDatabase)
✅ Face detection with mood saving (modified from Face module)
✅ Chatbot without login requirement (modified from MoodTune_c_main_db_login)
✅ Quick mood buttons on Home Page
✅ Sidebar menu with module_4 reports
✅ Settings page with all options
✅ Unified database handler for all mood sources
✅ Login persistence for returning users
✅ Navigation flow between all modules

### 9. Next Steps

1. Test all features thoroughly
2. Customize UI/UX as needed
3. Add error handling improvements
4. Add loading indicators where needed
5. Optimize performance
6. Add unit tests
7. Prepare for release

