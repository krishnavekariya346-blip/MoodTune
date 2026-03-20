# Module Integration Guide - MoodTune App

## 🏗️ Overview
This document explains how all modules in the MoodTune app are integrated together, including navigation flows, data sharing, and architectural patterns.

---

## 📐 Architecture Pattern

### **Type**: Activity-Based Architecture with Centralized Services

The app uses:
- **Activities** for UI screens
- **Shared Services** for business logic
- **Firebase** for data storage and authentication
- **Intent-based Navigation** for module communication

---

## 🔄 Integration Flow Diagram

```
App Launch
    ↓
LoginActivity (Entry Point)
    ↓
    ├─→ Check Firebase Auth → Already logged in?
    │   ├─→ Yes → HomePageActivity
    │   └─→ No → Google Sign-In → HomePageActivity
    ↓
HomePageActivity (Central Hub)
    ↓
    ├─→ Face Detection → FaceDetectionActivity
    │   └─→ Mood Detected → Save to DB → HomeActivity (Music)
    │
    ├─→ Chatbot → ChatbotActivity
    │   └─→ Mood Detected → Save to DB → (No auto-navigation)
    │
    ├─→ Quick Mood Buttons → Save to DB → HomeActivity (Music)
    │
    ├─→ Navigation Drawer
    │   ├─→ Daily Report → DailyReportActivity
    │   ├─→ Weekly Report → WeeklyReportActivity
    │   ├─→ Monthly Report → MonthlyReportActivity
    │   └─→ Settings → SettingsActivity
    │       ├─→ Edit Profile → EditProfileActivity
    │       ├─→ Change Password → ChangePasswordActivity
    │       ├─→ About → AboutActivity
    │       └─→ Sign Out → LoginActivity
    │
    └─→ Music Player → HomeActivity
        └─→ Change Mood → MoodSelectionActivity → HomeActivity
```

---

## 🔗 Module Integration Points

### 1. **Authentication Integration**

#### Entry Point: `LoginActivity`
```java
// Location: app/src/main/java/com/moodtune/app/auth/LoginActivity.java

// Integration Flow:
1. App launches → LoginActivity (LAUNCHER activity)
2. Check FirebaseAuth.getCurrentUser()
3. If logged in → Navigate to HomePageActivity
4. If not → Google Sign-In flow
5. After sign-in → Create/update Firestore user profile
6. Navigate to HomePageActivity
```

**Integration Code:**
```java
// Lines 48-54: Check existing session
FirebaseUser currentUser = mAuth.getCurrentUser();
if (currentUser != null) {
    navigateToHomePage();
    return;
}

// Lines 243-255: Navigation to home
Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                Intent.FLAG_ACTIVITY_NEW_TASK | 
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
```

**Shared Components:**
- `FirebaseAuth` - Singleton instance used across all modules
- `FirebaseUser` - Current user state shared globally

---

### 2. **Home Page Integration (Central Hub)**

#### Location: `HomePageActivity`
```java
// Location: app/src/main/java/com/moodtune/app/home/HomePageActivity.java
```

**Integration Responsibilities:**
1. **Authentication Check** - Verifies user is logged in
2. **Navigation Hub** - Routes to all major modules
3. **Quick Mood Selection** - Direct mood input
4. **User Welcome** - Displays user info from Firebase

**Integration Points:**

#### A. Face Detection Integration
```java
// Lines 135-138
btnFaceDetection.setOnClickListener(v -> {
    Intent intent = new Intent(HomePageActivity.this, FaceDetectionActivity.class);
    startActivity(intent);
});
```

#### B. Chatbot Integration
```java
// Lines 141-144
btnChatbot.setOnClickListener(v -> {
    Intent intent = new Intent(HomePageActivity.this, ChatbotActivity.class);
    startActivity(intent);
});
```

#### C. Quick Mood Buttons Integration
```java
// Lines 147-151
btnHappy.setOnClickListener(v -> handleQuickMood("happy"));
btnSad.setOnClickListener(v -> handleQuickMood("sad"));
// ... other moods

// Lines 154-165: Handle quick mood
private void handleQuickMood(String mood) {
    moodDatabaseHandler.saveMoodFromQuickButton(mood);
    Intent intent = new Intent(HomePageActivity.this, HomeActivity.class);
    intent.putExtra("selected_mood", mood);
    startActivity(intent);
}
```

#### D. Navigation Drawer Integration
```java
// Lines 115-130
navigationView.setNavigationItemSelectedListener(item -> {
    if (itemId == R.id.menu_weekly_summary) {
        startActivity(new Intent(HomePageActivity.this, WeeklyReportActivity.class));
    } else if (itemId == R.id.menu_mood_chart) {
        startActivity(new Intent(HomePageActivity.this, DailyReportActivity.class));
    } else if (itemId == R.id.menu_mood_history) {
        startActivity(new Intent(HomePageActivity.this, MonthlyReportActivity.class));
    } else if (itemId == R.id.menu_settings) {
        startActivity(new Intent(HomePageActivity.this, SettingsActivity.class));
    }
    return true;
});
```

---

### 3. **Mood Detection Integration (Unified Database Handler)**

#### Central Component: `MoodDatabaseHandler`
```java
// Location: app/src/main/java/com/moodtune/app/database/MoodDatabaseHandler.java
```

**Purpose**: Unified interface for saving moods from all sources

**Integration Pattern:**
```
All Mood Sources → MoodDatabaseHandler → Firebase Firestore
```

**Source-Specific Methods:**

#### A. Face Detection Integration
```java
// FaceDetectionActivity.java (Line 197)
moodDatabaseHandler.saveMoodFromFace(emotion, confidence);

// MoodDatabaseHandler.java (Lines 84-86)
public void saveMoodFromFace(String mood, double confidence) {
    saveMood(mood, confidence, "face");
}
```

#### B. Chatbot Integration
```java
// ChatbotActivity.java (Line 159)
moodDatabaseHandler.saveMoodFromChatbot(mood, conf);

// MoodDatabaseHandler.java (Lines 91-101)
public void saveMoodFromChatbot(String mood, String confidence) {
    Double conf = parseConfidence(confidence);
    saveMood(mood, conf, "chatbot");
}
```

#### C. Quick Mood Integration
```java
// HomePageActivity.java (Line 156)
moodDatabaseHandler.saveMoodFromQuickButton(mood);

// MoodDatabaseHandler.java (Lines 106-108)
public void saveMoodFromQuickButton(String mood) {
    saveMood(mood, null, "quick_mood");
}
```

**Unified Storage Structure:**
```java
// Lines 38-79: Core save method
public void saveMood(String mood, Double confidence, String source) {
    // Creates mood entry with:
    // - mood: "happy/sad/angry/neutral/surprise"
    // - date: UTC timestamp
    // - source: "face/chatbot/quick_mood"
    // - confidence: optional double
    
    // Updates:
    // 1. users/{userId} - currentMood
    // 2. moods/{userId}/history[] - mood entry
}
```

---

### 4. **Face Detection → Music Integration**

#### Flow:
```
FaceDetectionActivity → Mood Detected → Save to DB → Navigate to Music
```

**Integration Code:**
```java
// FaceDetectionActivity.java (Lines 188-206)
private void onAnalysisSuccess(String emotion, double confidence) {
    // Save mood
    moodDatabaseHandler.saveMoodFromFace(emotion, confidence);
    
    // Navigate to music with mood
    Intent intent = new Intent(FaceDetectionActivity.this, HomeActivity.class);
    intent.putExtra("selected_mood", emotion);
    startActivity(intent);
    finish();
}
```

**Data Passing:**
- **Method**: Intent Extras
- **Key**: `"selected_mood"`
- **Value**: Detected emotion string

---

### 5. **Chatbot → Database Integration**

#### Flow:
```
ChatbotActivity → User Message → Backend API → Mood Detected → Save to DB
```

**Integration Code:**
```java
// ChatbotActivity.java (Lines 132-165)
private void sendToBackend(String text) {
    // Send to backend API
    String url = BASE_URL + "/predict";
    // ... HTTP request
    
    // Parse response
    String mood = jo.get("mood").getAsString();
    String conf = jo.get("confidence").getAsString();
    
    // Save to database
    moodDatabaseHandler.saveMoodFromChatbot(mood, conf);
    
    // Display result (no auto-navigation to music)
}
```

**Note**: Chatbot does NOT auto-navigate to music (unlike Face Detection)

---

### 6. **Music Module Integration**

#### Location: `HomeActivity` (Music Player)

**Integration Points:**

#### A. Receiving Mood from Other Modules
```java
// HomeActivity.java (Lines 70-74)
String selectedMood = getIntent().getStringExtra("selected_mood");
if (selectedMood != null) {
    userMood = selectedMood;
}
```

**Mood Sources:**
1. Face Detection → Intent extra
2. Quick Mood Buttons → Intent extra
3. Mood Selection Activity → Intent extra
4. Firestore → User's current mood (fallback)

#### B. Music Service Integration
```java
// HomeActivity.java uses:
// - PlaylistService: Fetches playlists from Firestore
// - AudiusApiService: Fetches tracks from Audius API
// - ExoPlayer: Plays audio streams
```

**Service Chain:**
```
HomeActivity → PlaylistService → AudiusApiService → Audius API
                ↓
            Firestore (user playlists)
```

#### C. Mood Selection Integration
```java
// HomeActivity.java (Lines 91-94)
btnChangeMood.setOnClickListener(v -> {
    Intent intent = new Intent(HomeActivity.this, MoodSelectionActivity.class);
    startActivity(intent);
});

// MoodSelectionActivity.java (Lines 85-88)
Intent intent = new Intent(MoodSelectionActivity.this, HomeActivity.class);
intent.putExtra("selected_mood", mood);
startActivity(intent);
```

---

### 7. **Reports Module Integration**

#### Data Source: Firebase Firestore

**Integration Pattern:**
```
Reports Activities → Firebase Firestore → Mood History → Display Charts/Calendar
```

#### A. Daily Report Integration
```java
// DailyReportActivity.java (Lines 80-148)
// Fetches from: db.collection("moods").document(userId)
// Processes: mood history array
// Displays: MaterialCalendarView with mood dots
```

#### B. Weekly Report Integration
```java
// WeeklyReportActivity.java
// Fetches: Last 7 days from mood history
// Displays: Pie chart (MPAndroidChart)
```

#### C. Monthly Report Integration
```java
// MonthlyReportActivity.java
// Fetches: Last 30 days from mood history
// Displays: Bar chart (MPAndroidChart)
```

**Shared Data Access:**
- All reports use same Firestore collection: `moods/{userId}`
- All filter by `userId` from `FirebaseAuth.getCurrentUser()`
- All process `history` array from mood document

---

### 8. **Settings Module Integration**

#### Location: `SettingsActivity`

**Integration Points:**

#### A. Navigation to Sub-Settings
```java
// SettingsActivity.java
layoutEditProfile → EditProfileActivity
layoutChangePassword → ChangePasswordActivity
layoutAbout → AboutActivity
layoutSignOut → Sign out flow
```

#### B. Sign Out Integration
```java
// SettingsActivity.java (Lines 100-118)
private void signOut() {
    // 1. Sign out from Firebase
    mAuth.signOut();
    
    // 2. Sign out from Google
    googleSignInClient.signOut();
    
    // 3. Clear preferences
    prefs.edit().putBoolean("isLoggedIn", false).apply();
    
    // 4. Navigate to login
    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                    Intent.FLAG_ACTIVITY_NEW_TASK | 
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

#### C. Profile Management Integration
```java
// EditProfileActivity.java
// Reads: FirebaseUser.getDisplayName(), getEmail()
// Updates: Firestore users/{userId} collection
```

---

## 🔌 Shared Services & Components

### 1. **Firebase Services (Singleton Pattern)**

#### FirebaseAuth
```java
// Used in ALL modules
FirebaseAuth mAuth = FirebaseAuth.getInstance();
FirebaseUser currentUser = mAuth.getCurrentUser();
```

**Modules Using:**
- LoginActivity
- HomePageActivity
- ChatbotActivity
- FaceDetectionActivity
- All Reports Activities
- All Settings Activities
- MoodDatabaseHandler

#### FirebaseFirestore
```java
// Used in ALL modules that need data
FirebaseFirestore db = FirebaseFirestore.getInstance();
```

**Modules Using:**
- LoginActivity (user profile)
- MoodDatabaseHandler (mood storage)
- PlaylistService (playlist management)
- All Reports Activities (mood history)
- EditProfileActivity (profile updates)

### 2. **MoodDatabaseHandler (Shared Service)**

**Usage Pattern:**
```java
// Instantiated in each activity that needs it
MoodDatabaseHandler moodDatabaseHandler = new MoodDatabaseHandler();

// Used by:
// - HomePageActivity (quick mood)
// - FaceDetectionActivity (face mood)
// - ChatbotActivity (chatbot mood)
```

**Benefits:**
- Unified mood storage interface
- Consistent data structure
- Source tracking (face/chatbot/quick_mood)
- Centralized error handling

### 3. **Music Services**

#### PlaylistService
```java
// Used by: HomeActivity
PlaylistService playlistService = new PlaylistService(context);
playlistService.createGeneralPlaylistForMood(userId, mood, callback);
```

#### AudiusApiService
```java
// Used by: PlaylistService
AudiusApiService audiusApiService = new AudiusApiService(context);
audiusApiService.fetchTracksForMood(mood, callback);
```

---

## 📊 Data Flow Architecture

### **Centralized Data Storage: Firebase Firestore**

```
┌─────────────────────────────────────────────────┐
│           Firebase Firestore                    │
├─────────────────────────────────────────────────┤
│  Collections:                                   │
│  - users/{userId}                               │
│    └─ currentMood, name, email                  │
│                                                  │
│  - moods/{userId}                               │
│    └─ history[] (array of mood entries)         │
│       ├─ mood, date, source, confidence         │
│                                                  │
│  - playlists/{playlistId}                       │
│    └─ userId, mood, songs[]                     │
└─────────────────────────────────────────────────┘
         ↑                    ↑
         │                    │
    Write Operations    Read Operations
         │                    │
    ┌────┴────┐         ┌─────┴─────┐
    │         │         │           │
MoodDatabase  Playlist  Reports   Profile
  Handler     Service  Activities  Edit
```

---

## 🔄 Complete Integration Example

### **Scenario: User detects mood via Face Detection**

```
1. User opens app
   └─→ LoginActivity checks FirebaseAuth
       └─→ Already logged in → HomePageActivity

2. User clicks "Face Detection" button
   └─→ HomePageActivity → Intent → FaceDetectionActivity

3. User captures photo
   └─→ FaceDetectionActivity → HTTP POST → Backend API (port 5000)
       └─→ Backend processes image → Returns {emotion, confidence}

4. FaceDetectionActivity receives response
   └─→ moodDatabaseHandler.saveMoodFromFace(emotion, confidence)
       └─→ MoodDatabaseHandler.saveMood(mood, confidence, "face")
           └─→ Firebase Firestore:
               ├─→ users/{userId} → currentMood = emotion
               └─→ moods/{userId}/history[] → Add mood entry

5. Navigate to Music
   └─→ Intent → HomeActivity
       └─→ Intent extra: "selected_mood" = emotion

6. HomeActivity loads music
   └─→ PlaylistService.createGeneralPlaylistForMood()
       └─→ AudiusApiService.fetchTracksForMood(emotion)
           └─→ Audius API search → Returns tracks
               └─→ ExoPlayer plays tracks

7. User views reports later
   └─→ DailyReportActivity
       └─→ Firebase Firestore → moods/{userId}/history[]
           └─→ Processes mood entries → Displays on calendar
```

---

## 🎯 Integration Patterns Used

### 1. **Intent-Based Navigation**
```java
// Pattern: Activity A → Activity B
Intent intent = new Intent(ActivityA.this, ActivityB.class);
intent.putExtra("key", value);  // Optional data passing
startActivity(intent);
```

**Used For:**
- All activity transitions
- Passing mood data between modules
- Navigation drawer items

### 2. **Shared Service Pattern**
```java
// Pattern: Multiple modules use same service
MoodDatabaseHandler handler = new MoodDatabaseHandler();
handler.saveMoodFromFace(...);
handler.saveMoodFromChatbot(...);
handler.saveMoodFromQuickButton(...);
```

**Used For:**
- MoodDatabaseHandler (unified mood storage)
- Firebase services (Auth, Firestore)
- Music services (PlaylistService, AudiusApiService)

### 3. **Singleton Pattern**
```java
// Pattern: Single instance shared across app
FirebaseAuth.getInstance();
FirebaseFirestore.getInstance();
```

**Used For:**
- Firebase services
- Application-level configuration

### 4. **Callback Pattern**
```java
// Pattern: Async operations with callbacks
audiusApiService.fetchTracksForMood(mood, new TracksCallback() {
    @Override
    void onSuccess(List<Song> songs) { ... }
    @Override
    void onError(String error) { ... }
});
```

**Used For:**
- API calls (Audius, Backend APIs)
- Firestore operations
- Music playlist loading

---

## 🔐 Authentication Integration

### **Global Auth Check Pattern**

Every protected activity checks authentication:

```java
// Pattern used in all activities
FirebaseAuth mAuth = FirebaseAuth.getInstance();
FirebaseUser currentUser = mAuth.getCurrentUser();

if (currentUser == null) {
    // Redirect to login or finish
    finish();
    return;
}
```

**Activities with Auth Checks:**
- HomePageActivity
- ChatbotActivity
- FaceDetectionActivity
- HomeActivity (Music)
- All Reports Activities
- All Settings Activities

---

## 📦 Module Dependencies

### **Dependency Graph:**

```
LoginActivity
    └─→ FirebaseAuth
    └─→ FirebaseFirestore

HomePageActivity
    └─→ FirebaseAuth
    └─→ MoodDatabaseHandler
        └─→ FirebaseFirestore
        └─→ FirebaseAuth

FaceDetectionActivity
    └─→ MoodDatabaseHandler
    └─→ HomeActivity (via Intent)

ChatbotActivity
    └─→ MoodDatabaseHandler
    └─→ FirebaseAuth

HomeActivity (Music)
    └─→ PlaylistService
        └─→ AudiusApiService
    └─→ FirebaseFirestore
    └─→ ExoPlayer

Reports Activities
    └─→ FirebaseFirestore
    └─→ FirebaseAuth
    └─→ MPAndroidChart
    └─→ MaterialCalendarView

Settings Activities
    └─→ FirebaseAuth
    └─→ FirebaseFirestore
```

---

## 🛠️ Integration Best Practices Used

### 1. **Separation of Concerns**
- UI logic in Activities
- Business logic in Services
- Data access in Handlers

### 2. **Unified Data Interface**
- MoodDatabaseHandler provides single interface
- Consistent data structure across sources
- Source tracking for analytics

### 3. **Error Handling**
- Try-catch blocks in critical paths
- Graceful degradation (fallback to trending tracks)
- User-friendly error messages

### 4. **Data Consistency**
- UTC timestamps for all mood entries
- Batch writes for atomic operations
- Source tracking for each mood entry

---

## 📝 Key Integration Files

### **Core Integration Files:**

1. **MoodDatabaseHandler.java**
   - Central mood storage interface
   - Used by all mood detection sources

2. **MoodTuneApplication.java**
   - Application-level configuration
   - Theme management

3. **AndroidManifest.xml**
   - Activity declarations
   - Permissions
   - Entry point configuration

4. **HomePageActivity.java**
   - Central navigation hub
   - Routes to all major modules

---

## 🔄 Data Synchronization

### **Real-time Sync:**
- Firebase Firestore provides real-time updates
- No manual sync required
- Automatic conflict resolution

### **Offline Support:**
- Firebase Firestore has offline persistence
- Data syncs when connection restored
- Local cache for offline access

---

## 🎨 UI Integration

### **Material Design Components:**
- Consistent UI across modules
- Navigation Drawer for main menu
- CardView for mood buttons
- RecyclerView for lists

### **Theme Integration:**
- Centralized theme in MoodTuneApplication
- Dark/Light mode support
- SharedPreferences for persistence

---

## 📊 Summary

### **Integration Architecture:**

1. **Entry Point**: LoginActivity (checks auth)
2. **Central Hub**: HomePageActivity (routes to modules)
3. **Mood Sources**: Face, Chatbot, Quick Buttons
4. **Unified Storage**: MoodDatabaseHandler → Firestore
5. **Music Integration**: Receives mood → Fetches tracks → Plays
6. **Reports**: Read from Firestore → Display charts/calendar
7. **Settings**: Profile management and sign-out

### **Key Integration Points:**

- ✅ Intent-based navigation between activities
- ✅ Shared Firebase services (Auth, Firestore)
- ✅ Unified mood storage (MoodDatabaseHandler)
- ✅ Centralized data storage (Firestore)
- ✅ Consistent authentication checks
- ✅ Service-based architecture for business logic

---

*Last Updated: Based on current codebase analysis*

