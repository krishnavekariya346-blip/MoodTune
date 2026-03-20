# MoodTune App - Technical Overview

## 📱 Project Overview
**MoodTune** is an Android application that detects user moods through multiple methods (face detection, chatbot, manual selection) and recommends music based on detected emotions.

---

## 🛠️ Technologies Used

### Core Technologies
- **Language**: Java (primary), Kotlin (partial - Compose components)
- **Platform**: Android (Native)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Build System**: Gradle (Kotlin DSL)
- **Architecture**: Activity-based architecture

---

## 📚 Libraries & Dependencies

### UI/UX Libraries
1. **AndroidX AppCompat** (v1.7.1) - Backward compatibility
2. **Material Design Components** (v1.13.0) - Material UI components
3. **ConstraintLayout** (v2.2.1) - Flexible layout system
4. **RecyclerView** (v1.3.0) - Efficient list rendering
5. **CardView** (v1.0.0) - Card-based UI components
6. **Material Calendar View** (v1.4.3) - Calendar widget for mood tracking

### Firebase Services
1. **Firebase Authentication** (v23.0.0) - User authentication
2. **Firebase Firestore** (v25.1.1) - Cloud database
3. **Firebase Realtime Database** (v21.0.0) - Real-time data sync
4. **Firebase Analytics** (BOM v34.2.0) - Analytics tracking
5. **Google Play Services Auth** (v21.2.0) - Google Sign-In

### Networking Libraries
1. **OkHttp** (v4.12.0) - HTTP client
2. **Retrofit** (v2.9.0) - REST API client
3. **Retrofit Gson Converter** (v2.9.0) - JSON parsing
4. **OkHttp Logging Interceptor** (v4.12.0) - Network debugging

### Media Libraries
1. **ExoPlayer** (v2.19.1) - Audio/video streaming player

### Data Visualization
1. **MPAndroidChart** (v3.1.0) - Charts and graphs for mood reports

### Data Processing
1. **Gson** (v2.10.1) - JSON serialization/deserialization

### gRPC (for Firestore)
1. **gRPC Core** (v1.62.2) - RPC framework
2. **gRPC OkHttp** (v1.62.2) - OkHttp transport
3. **gRPC Android** (v1.62.2) - Android support
4. **gRPC API** (v1.62.2) - Core API
5. **gRPC Protobuf Lite** (v1.62.2) - Protocol buffers

---

## 🌐 APIs & External Services

### 1. **Audius API** (Music Streaming)
- **Endpoint**: `https://api.audius.co`
- **Purpose**: Fetch music tracks based on mood
- **Features**:
  - Track search by mood-based queries
  - Trending tracks (weekly/monthly)
  - Stream URL generation
  - Discovery node management
- **Mood-to-Query Mapping**:
  - Happy → "bollywood hindi party"
  - Sad → "bollywood hindi romantic"
  - Angry → "punjabi rap energetic"
  - Neutral → "hindi lofi chill"
  - Surprise → "bollywood remix mashup"

### 2. **Custom Backend API** (Face Detection)
- **Endpoint**: `http://10.59.109.122:5000/analyze` (Production)
- **Endpoint**: `http://10.0.2.2:5000/analyze` (Emulator)
- **Purpose**: Analyze facial expressions for emotion detection
- **Method**: POST
- **Request**: JPEG image bytes
- **Response**: JSON with `emotion` and `confidence` fields

### 3. **Custom Backend API** (Chatbot/Mood Detection)
- **Endpoint**: `http://10.59.109.122:5001/predict` (Production)
- **Endpoint**: `http://10.0.2.2:5001/predict` (Emulator)
- **Purpose**: Text-based mood detection from user messages
- **Method**: POST
- **Request**: JSON with `text` field
- **Response**: JSON with `mood` and `confidence` fields

---

## 📦 Module-wise Breakdown

### 1. **Authentication Module** (`auth/`)
- **Technology**: Firebase Authentication
- **Features**:
  - Email/password login
  - Google Sign-In integration
  - User session management
- **Files**: `LoginActivity.java`

### 2. **Home Module** (`home/`)
- **Technology**: Android Activities, Navigation Drawer
- **Features**:
  - Welcome screen with user name
  - Quick mood selection buttons (Happy, Sad, Angry, Neutral, Surprise)
  - Navigation drawer for reports and settings
  - Face detection and chatbot access
- **Libraries**: Material Navigation View, DrawerLayout
- **Files**: `HomePageActivity.java`

### 3. **Face Detection Module** (`face/`)
- **Technology**: Camera API, Custom ML Backend
- **Features**:
  - Camera capture
  - Image upload to backend
  - Emotion detection (Happy, Sad, Angry, Neutral, Surprise)
  - Confidence score display
- **APIs**: Custom backend at port 5000
- **Libraries**: OkHttp for HTTP requests
- **Files**: `FaceDetectionActivity.java`

### 4. **Chatbot Module** (`chatbot/`)
- **Technology**: Custom ML Backend, RecyclerView
- **Features**:
  - Chat interface
  - Text-based mood detection
  - Real-time mood prediction
  - Chat history display
- **APIs**: Custom backend at port 5001
- **Libraries**: OkHttp, Gson, RecyclerView
- **Files**: `ChatbotActivity.java`, `ChatAdapter.java`

### 5. **Music Module** (`music/`)
- **Technology**: ExoPlayer, Audius API
- **Features**:
  - Mood-based music recommendations
  - Audio streaming
  - Playlist management
  - Song playback controls
- **APIs**: Audius API
- **Libraries**: ExoPlayer, Retrofit, OkHttp
- **Files**: 
  - `HomeActivity.java` - Main music player
  - `MoodSelectionActivity.java` - Mood selection
  - `AudiusApiService.java` - API integration
  - `PlaylistService.java` - Playlist management
  - `SongAdapter.java` - Song list adapter

### 6. **Reports Module** (`reports/`)
- **Technology**: Firebase Firestore, Material Calendar View, MPAndroidChart
- **Features**:
  - Daily mood calendar view
  - Weekly mood summary
  - Monthly mood history
  - Mood statistics and charts
- **Libraries**: Material Calendar View, MPAndroidChart, Firebase Firestore
- **Files**:
  - `DailyReportActivity.java` - Calendar-based daily view
  - `WeeklyReportActivity.java` - Weekly statistics
  - `MonthlyReportActivity.java` - Monthly statistics
  - `MoodDotDecorator.java` - Calendar decoration

### 7. **Settings Module** (`settings/`)
- **Technology**: Firebase Authentication, Firebase Firestore
- **Features**:
  - Edit user profile
  - Change password
  - App information
  - User preferences
- **Files**:
  - `SettingsActivity.java`
  - `EditProfileActivity.java`
  - `ChangePasswordActivity.java`
  - `AboutActivity.java`

### 8. **Database Module** (`database/`)
- **Technology**: Firebase Firestore
- **Features**:
  - Unified mood storage
  - User data management
  - Mood history tracking
  - Source tracking (face, chatbot, quick_mood)
- **Files**: `MoodDatabaseHandler.java`

### 9. **Compose Module** (`compose/`)
- **Technology**: Jetpack Compose (Kotlin)
- **Features**:
  - Modern UI components
  - Dialog components
  - Music player UI
- **Files**:
  - `MoodSelectionCompose.kt`
  - `MoodTuneDialog.kt`
  - `MusicPlayerCompose.kt`

---

## 🔧 Technical Architecture

### Data Flow
1. **Mood Detection** → Face/Chatbot/Manual → `MoodDatabaseHandler` → Firebase Firestore
2. **Music Recommendation** → Mood → `AudiusApiService` → Track List → `ExoPlayer`
3. **Reports** → Firebase Firestore → Data Processing → Charts/Calendar

### Backend Integration
- **Face Detection**: Python ML backend (port 5000)
- **Chatbot**: Python NLP backend (port 5001)
- Both backends use RESTful APIs with JSON responses

### Storage
- **Cloud**: Firebase Firestore (user data, mood history)
- **Local**: No local database (all data in cloud)

---

## ❓ Additional Technical Questions & Answers

### Q1: How does the app handle offline scenarios?
**A**: The app primarily relies on Firebase Firestore which has offline persistence capabilities. However, music streaming requires internet connectivity. Mood data is stored in Firestore which syncs when connection is restored.

### Q2: What authentication methods are supported?
**A**: 
- Email/Password authentication via Firebase Auth
- Google Sign-In via Google Play Services Auth
- Session persistence through Firebase Auth state

### Q3: How is mood data structured in Firestore?
**A**: 
- Collection: `users` - User profile and current mood
- Collection: `moods` - Mood history with entries containing:
  - `mood`: String (happy, sad, angry, neutral, surprise)
  - `date`: ISO 8601 timestamp
  - `source`: String (face, chatbot, quick_mood)
  - `confidence`: Double (optional, for face detection)

### Q4: How does the music recommendation algorithm work?
**A**: 
1. Mood is mapped to search queries (e.g., "happy" → "bollywood hindi party")
2. Audius API is queried with mood-specific search terms
3. Results are filtered for streamable tracks
4. Tracks are shuffled and presented as playlist
5. Fallback to trending tracks if mood-specific search fails

### Q5: What permissions does the app require?
**A**: 
- `INTERNET` - Network access for APIs and streaming
- `ACCESS_NETWORK_STATE` - Check network connectivity
- `CAMERA` - Face detection feature (optional)

### Q6: How is audio streaming implemented?
**A**: 
- Uses ExoPlayer library for audio playback
- Streams directly from Audius API stream URLs
- Supports play/pause/seek controls
- Handles playback errors and retries

### Q7: What is the app's data synchronization strategy?
**A**: 
- Real-time sync via Firebase Firestore
- Batch writes for mood entries
- Automatic retry on network failures
- UTC timestamps for consistent timezone handling

### Q8: How does the face detection backend work?
**A**: 
- App captures photo using Camera API
- Image is compressed to JPEG (90% quality)
- Sent as POST request to backend `/analyze` endpoint
- Backend returns emotion classification with confidence score
- Response is parsed and mood is saved to Firestore

### Q9: What UI/UX patterns are used?
**A**: 
- Material Design 3 components
- Navigation Drawer for main menu
- RecyclerView for lists (songs, chat messages)
- CardView for mood selection buttons
- Calendar view for mood history visualization
- Animations for welcome text and emoji buttons

### Q10: How is error handling implemented?
**A**: 
- Network errors: Toast messages and error callbacks
- API failures: Fallback to alternative endpoints (trending tracks)
- Permission denials: User-friendly permission requests
- Firebase errors: Logging and graceful degradation

### Q11: What build configuration is used?
**A**: 
- Gradle Kotlin DSL (`.gradle.kts` files)
- Version catalog (`libs.versions.toml`) for dependency management
- Google Services plugin for Firebase integration
- ProGuard disabled for debug builds

### Q12: How are different moods mapped to music genres?
**A**: 
- **Happy**: Bollywood Hindi party music
- **Sad**: Bollywood Hindi romantic songs
- **Angry**: Punjabi rap and energetic tracks
- **Neutral**: Hindi lofi and chill music
- **Surprise**: Bollywood remix and mashup tracks

---

## 📊 Summary Statistics

- **Total Activities**: 11
- **Main Modules**: 9
- **External APIs**: 3 (Audius, Face Detection Backend, Chatbot Backend)
- **Firebase Services**: 4 (Auth, Firestore, Database, Analytics)
- **Third-party Libraries**: 20+
- **Supported Moods**: 5 (Happy, Sad, Angry, Neutral, Surprise)
- **Data Sources**: 3 (Face Detection, Chatbot, Manual Selection)

---

## 🔐 Security Considerations

1. **Authentication**: Firebase Auth handles secure user authentication
2. **Network**: HTTPS for Audius API, HTTP for local backends (development)
3. **Data**: User data stored securely in Firebase Firestore
4. **Permissions**: Runtime permission requests for camera access

---

## 🚀 Performance Optimizations

1. **Caching**: Audius discovery host caching
2. **Threading**: ExecutorService for background API calls
3. **Image Compression**: JPEG compression (90%) before upload
4. **Lazy Loading**: RecyclerView for efficient list rendering
5. **Batch Writes**: Firebase batch operations for mood storage

---

*Last Updated: Based on current codebase analysis*

