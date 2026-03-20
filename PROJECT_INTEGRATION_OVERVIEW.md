# MoodTune - Project Integration Overview

## 📱 Project Overview
**MoodTune** is an Android application that detects user moods through multiple methods (face detection, chatbot interaction, and quick mood buttons) and provides personalized music recommendations based on the detected mood.

---

## 🎯 Key Features

1. **Multi-Method Mood Detection**
   - Face Detection (Camera-based emotion recognition)
   - Chatbot Interaction (Natural language processing)
   - Quick Mood Buttons (Manual selection)

2. **Music Recommendation System**
   - Integration with Audius API
   - Mood-based playlist generation
   - Music playback functionality

3. **Comprehensive Reporting System**
   - Daily Mood Calendar (Dominant mood per day)
   - Weekly Mood Distribution (Pie Chart)
   - Today's Mood Analysis (Bar Chart)
   - Monthly Average Mood Calculation

4. **User Authentication**
   - Google Sign-In integration
   - Firebase Authentication

---

## 🛠 Technology Stack

### **Frontend**
- **Language**: Java
- **UI Framework**: Android SDK
- **UI Components**: 
  - Material Design Components
  - Navigation Drawer
  - RecyclerView, CardView
  - Material Calendar View

### **Backend & Services**
- **Firebase Services**:
  - Firebase Authentication (Google Sign-In)
  - Cloud Firestore (Database)
  - Firebase Storage (if needed)

### **Third-Party Libraries**
- **MPAndroidChart**: For pie charts and bar charts
- **MaterialCalendarView**: For calendar display
- **ExoPlayer**: For music playback
- **Google Play Services**: For authentication

### **APIs**
- **Audius API**: For music streaming and playlist generation

---

## 🏗 Architecture & Integration Points

### **1. Authentication Integration**
```
User → Google Sign-In → Firebase Auth → User Profile Creation → Home Screen
```
- **Integration**: Google Sign-In SDK + Firebase Authentication
- **Data Flow**: OAuth 2.0 → Firebase Auth Token → User UID
- **Storage**: User profile stored in Firestore

### **2. Mood Detection Integration**

#### **A. Face Detection Module**
```
Camera → Image Capture → ML Model Processing → Emotion Detection → Mood Saved
```
- **Technology**: Android Camera API + ML Kit (or custom ML model)
- **Output**: Emotion classification (Happy, Sad, Angry, Neutral, Surprise)
- **Storage**: Mood entry with source="face" in Firestore

#### **B. Chatbot Module**
```
User Input → NLP Processing → Sentiment Analysis → Mood Detection → Response + Mood Saved
```
- **Technology**: Natural Language Processing
- **Output**: Mood classification based on conversation sentiment
- **Storage**: Mood entry with source="chatbot" in Firestore

#### **C. Quick Mood Buttons**
```
Button Click → Mood Selection → Immediate Save → Music Recommendation
```
- **Technology**: Simple UI interaction
- **Output**: Direct mood selection
- **Storage**: Mood entry with source="quick_mood" in Firestore

### **3. Data Storage Integration (Firestore)**
```
Mood Entry Structure:
{
  "mood": "happy/sad/angry/neutral/surprise",
  "date": "2025-11-15T10:30:00Z",
  "source": "face/chatbot/quick_mood",
  "confidence": 0.85 (optional)
}
```
- **Collection**: `moods/{userId}/history[]`
- **User Collection**: `users/{userId}` (profile data)

### **4. Music Recommendation Integration**
```
Mood Detected → Audius API Call → Playlist Fetch → Display & Play
```
- **API**: Audius REST API
- **Integration**: HTTP requests to Audius endpoints
- **Playback**: ExoPlayer for streaming
- **Flow**: Mood → API Query → Track List → Player

### **5. Reporting System Integration**

#### **A. Daily Report (Calendar)**
- **Data Source**: Firestore mood history
- **Processing**: Count moods per day → Find dominant mood
- **Display**: MaterialCalendarView with colored dots

#### **B. Weekly Report (Pie Chart)**
- **Data Source**: Last 7 days from Firestore
- **Processing**: Count all moods → Calculate percentages
- **Display**: MPAndroidChart PieChart

#### **C. Monthly Report (Bar Chart)**
- **Data Source**: Today's moods from Firestore
- **Processing**: Count all moods detected today → Calculate percentages
- **Display**: MPAndroidChart BarChart

---

## 🔄 Data Flow Architecture

```
┌─────────────┐
│   User      │
└──────┬──────┘
       │
       ├──→ [Authentication] ──→ Firebase Auth ──→ User Profile
       │
       ├──→ [Mood Detection]
       │       ├──→ Face Detection ──→ ML Processing ──→ Mood
       │       ├──→ Chatbot ──→ NLP ──→ Mood
       │       └──→ Quick Buttons ──→ Mood
       │
       └──→ [Mood Storage] ──→ Firestore ──→ History Array
                                    │
                                    ├──→ [Reports]
                                    │       ├──→ Daily Calendar
                                    │       ├──→ Weekly Pie Chart
                                    │       └──→ Monthly Bar Chart
                                    │
                                    └──→ [Music Recommendation]
                                            └──→ Audius API ──→ Playlist ──→ ExoPlayer
```

---

## 🔑 Key Integration Components

### **1. Firebase Integration**
- **Authentication**: Google Sign-In flow
- **Firestore**: Real-time database for mood history
- **Structure**: 
  - `users/{userId}` - User profiles
  - `moods/{userId}` - Mood history array

### **2. Mood Priority System**
- **Priority Order**: 
  1. Quick Mood (Priority 3) - Highest
  2. Face Detection (Priority 2)
  3. Chatbot (Priority 1) - Lowest
- **Logic**: When multiple moods exist for same day, highest priority is selected

### **3. Chart Integration**
- **Library**: MPAndroidChart
- **Charts**: Pie Chart (Weekly), Bar Chart (Daily)
- **Data Processing**: Firestore → Aggregation → Chart Data

### **4. Calendar Integration**
- **Library**: MaterialCalendarView
- **Features**: 
  - Colored dots for mood indicators
  - Date selection for mood details
  - Monthly average mood calculation

### **5. Music Streaming Integration**
- **API**: Audius API
- **Player**: ExoPlayer
- **Features**: 
  - Mood-based playlist generation
  - Track playback
  - Playlist management

---

## 📊 Database Schema

### **Users Collection**
```json
{
  "userId": "user123",
  "displayName": "John Doe",
  "email": "john@example.com",
  "currentMood": "happy",
  "isFirstTime": false
}
```

### **Moods Collection**
```json
{
  "userId": "user123",
  "history": [
    {
      "mood": "happy",
      "date": "2025-11-15T10:30:00Z",
      "source": "quick_mood",
      "confidence": null
    },
    {
      "mood": "sad",
      "date": "2025-11-15T14:20:00Z",
      "source": "face",
      "confidence": 0.87
    }
  ]
}
```

---

## 🎵 Music Recommendation Flow

1. **Mood Detection** → User mood is identified
2. **API Request** → Query Audius API with mood parameter
3. **Playlist Generation** → API returns mood-appropriate tracks
4. **Display** → Show tracks in RecyclerView
5. **Playback** → User selects track → ExoPlayer streams audio

---

## 🔐 Security & Privacy

- **Authentication**: Secure Google OAuth 2.0
- **Data Privacy**: User data isolated by userId
- **Permissions**: Camera permission for face detection
- **Network Security**: HTTPS for all API calls

---

## 📈 Reporting Features

### **Daily Report (Calendar)**
- Shows dominant mood per day
- Calculates most frequently detected mood
- Visual indicators with colored dots

### **Weekly Report (Pie Chart)**
- Last 7 days mood distribution
- Percentage breakdown of all moods
- Visual pie chart representation

### **Monthly Report (Bar Chart)**
- Today's mood percentages
- All moods detected today
- Bar chart visualization

### **Monthly Average**
- Calculates most detected mood in current month
- Button-triggered calculation
- Emoji display of dominant mood

---

## 🚀 Key Technical Highlights

1. **Multi-Source Mood Detection**: Three different methods for comprehensive mood tracking
2. **Priority-Based Mood Selection**: Intelligent mood selection when multiple sources exist
3. **Real-Time Data Sync**: Firestore for instant data updates
4. **Visual Analytics**: Multiple chart types for mood visualization
5. **Personalized Music**: Mood-based music recommendation system
6. **User-Friendly UI**: Material Design with intuitive navigation

---

## 💡 Integration Challenges Solved

1. **Multiple Mood Sources**: Implemented priority system to handle conflicts
2. **Date/Time Handling**: UTC timezone consistency across all modules
3. **Chart Data Processing**: Efficient aggregation of Firestore data
4. **Music API Integration**: Seamless Audius API integration with error handling
5. **Calendar Display**: Custom decorators for mood visualization

---

## 📱 User Journey

1. **Login** → Google Sign-In
2. **Home Screen** → Choose mood detection method
3. **Mood Detection** → Face/Chatbot/Button
4. **Music Recommendation** → Get personalized playlist
5. **View Reports** → Check mood history and analytics
6. **Settings** → Manage profile and preferences

---

## 🎓 Viva Presentation Points

1. **Problem Statement**: Need for mood-based music recommendation
2. **Solution**: Multi-method mood detection + personalized music
3. **Technology**: Firebase, Android SDK, Third-party APIs
4. **Innovation**: Priority-based mood selection system
5. **Results**: Comprehensive mood tracking and analytics
6. **Future Scope**: Machine learning improvements, social features

---

## 📝 Summary

MoodTune integrates multiple technologies to create a comprehensive mood tracking and music recommendation system. The app uses Firebase for backend services, implements multiple mood detection methods, provides detailed analytics through charts and calendar, and offers personalized music recommendations through Audius API integration.


