# 🎵 MoodTune  

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Java-blue)
![Backend](https://img.shields.io/badge/Backend-Flask-orange)
![ML](https://img.shields.io/badge/ML-TensorFlow-red)
![Database](https://img.shields.io/badge/Database-Firebase-yellow)
![License](https://img.shields.io/badge/License-MIT-purple)

### AI-Powered Emotion-Based Music Recommender

> *“Your mood. Your music. Instantly.”*

---

## 📌 Overview  

MoodTune is a unified Android application that intelligently recommends music based on a user's emotional state.  

It combines **Facial Emotion Detection**, **Chat-based Sentiment Analysis**, and **Context-Aware Music Recommendation** to deliver a personalized and emotionally adaptive experience.

The app also tracks emotional patterns over time, helping users gain **self-awareness and mental wellness insights**.

---

## 🚀 Key Features  

### 🔐 Authentication  
- Google Sign-In using Firebase Authentication  
- Persistent login sessions  
- Automatic user profile creation  

---

### 😊 Mood Detection  
- 📷 Face Detection using ML model  
- 💬 Chatbot Emotion Detection (text-based)  
- ⚡ Quick Mood Selection Buttons  
  - Happy | Sad | Angry | Neutral | Surprise  

---

### 🎶 Music Recommendation  
- Emotion → Genre mapping  
- Songs fetched using Audius API / YouTube API  
- Built-in audio player using ExoPlayer  
- Personalized playlists for each mood  

---

### 📊 Mood Analytics & Reports  
- 📅 Daily Mood Calendar View  
- 🥧 Weekly Mood Distribution (Pie Chart)  
- 📈 Monthly Mood Trends (Bar Graph)  
- Emotion tracking with timestamps  

---

### ⚙️ Settings & Personalization  
- Edit Profile  
- Change Password  
- Theme Settings  
- Notifications  
- About Section  
- Secure Logout  

---

## 🧠 How It Works  

1. User logs in via Google Authentication  
2. Emotion is detected using:
   - Facial Expression (ML Model)
   - Chat Input (NLP-based detection)
3. Emotion is mapped to a music genre  
4. Songs are fetched dynamically via APIs  
5. User mood is stored and tracked in Firebase  
6. Reports are generated for insights  

---

## 🏗️ Project Structure  

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
---

## 🛠️ Tech Stack  

### 📱 Frontend  
- Android Studio (Java)  

### ⚙️ Backend  
- Flask / Django  

### 🤖 Machine Learning  
- TensorFlow / Keras  
- OpenCV  
- CNN (FER2013 Dataset)  

### ☁️ Database  
- Firebase Firestore  

### 🔌 APIs  
- Audius API   
- Weather API  
- Location Services  

---

## ⚙️ Setup Instructions  

### 1️⃣ Firebase Setup  
- Add `google-services.json` to `app/`  
- Update `default_web_client_id` in `strings.xml`  

---

### 2️⃣ Backend Configuration  

Update API URLs:

**Face Detection:**
```
http://<YOUR_IP>:5000/analyze
```

---

### 3️⃣ Build & Run  
```
./gradlew build
```


---

## 🗄️ Database Schema  

### 🔹 Users Collection  
```
{
  "uid": "user_id",
  "name": "User Name",
  "email": "user@email.com",
  "currentMood": "happy",
  "createdAt": 1234567890
}
```
### 🔹 Mood History
```
{
  "mood": "happy",
  "date": "2025-01-15T10:30:00Z",
  "source": "face/chatbot",
  "confidence": 0.85
}
```
### 🔹 Playlists
```
{
  "playlistId": "mood_user_timestamp",
  "mood": "happy",
  "songs": [...]
}
```

---

### 🔄 App Flow

- New User → Login → Home
- Returning User → Auto Login → Home
- Detect Mood → Store → Recommend Music
- View Reports → Analyze Mood Trends
- Settings → Personalization

---

### ⚠️ Challenges Faced

- Lighting issues in facial detection
- Similar expressions (Neutral vs Sad)
- Real-time API response handling
- Emotion accuracy from text input

---

### 🌱 Future Enhancements

- 🎧 Spotify / Apple Music Integration
- 🎤 Voice-based Emotion Detection
- ⌚ Smartwatch Integration
- 🧠 Advanced AI Emotion Models

---

## 📸 Screenshots  

### 🔐 Sign In Screen  
<p align="left">
  <img src="screenshots/home11.jpg" width="220"/>
  <img src="screenshots/home12.jpg" width="220"/>
</p>

---

### 🏠 Home Dashboard  
<p align="left">
  <img src="screenshots/home1.jpg" width="220"/>
</p>

---

### 😊 Face Detection  
<p align="left">
  <img src="screenshots/FaceDetection1.jpg" width="220"/>
  <img src="screenshots/FaceDetection2.jpg" width="220"/>
  <img src="screenshots/FaceDetection3.jpg" width="220"/>
</p>

---

### 💬 Chatbot  
<p align="left">
  <img src="screenshots/chatbot1.jpg" width="220"/>
  <img src="screenshots/chatbot2.jpg" width="220"/>
</p>

---

### 🎶 Music Player  
<p align="left">
  <img src="screenshots/song_happy.jpg" width="220"/>
  <img src="screenshots/song_neutral.jpg" width="220"/>
  <img src="screenshots/song_sad.jpg" width="220"/>
</p>

---

### 🌙 Dark Theme  
<p align="left">
  <img src="screenshots/dark1.jpg" width="220"/>
  <img src="screenshots/dark2.jpg" width="220"/>
  <img src="screenshots/dark3.jpg" width="220"/>
</p>

<p align="left">
  <img src="screenshots/dark4.jpg" width="220"/>
</p>

---

### 📂 Side View  
<p align="left">
  <img src="screenshots/sidebar.jpg" width="220"/>
</p>

---

### 📅 Daily Report / Calendar  
<p align="left">
  <img src="screenshots/calendar1.jpg" width="220"/>
  <img src="screenshots/calendar2.jpg" width="220"/>
  <img src="screenshots/calendar3.jpg" width="220"/>
</p>

---

### 📊 Monthly Report  
<p align="left">
  <img src="screenshots/chart_bar.jpg" width="220"/>
</p>

---

### 🥧 Weekly Report  
<p align="left">
  <img src="screenshots/chart_pie.jpg" width="220"/>
</p>

---

### ⚙️ Settings  
<p align="left">
  <img src="screenshots/settings.jpg" width="220"/>
  <img src="screenshots/edit.jpg" width="220"/>
  <img src="screenshots/changepwd.jpg" width="220"/>
</p>

<p align="left">
  <img src="screenshots/themes.jpg" width="220"/>
  <img src="screenshots/about.jpg" width="220"/>
</p>

---

### 💡 Use Cases

- Mood-based music listening
- Stress relief & mental wellness
- Emotional tracking & analysis
- Personalized entertainment

---

### 👨‍💻 Author

Krishna

