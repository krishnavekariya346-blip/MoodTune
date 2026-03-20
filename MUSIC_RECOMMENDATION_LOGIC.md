# Music Recommendation Logic - MoodTune App

## 🎵 Overview
This document explains how the app recommends music based on user moods. The app uses a **matching mood strategy** - it recommends music that **matches** your current emotional state, not opposite moods.

---

## 🎭 Mood-to-Music Mapping

### Current Strategy: **Matching Mood** (Not Opposite)

The app recommends music that **aligns with** your current mood:

| User Mood | Recommended Music Type | Search Query | Music Characteristics |
|-----------|----------------------|--------------|----------------------|
| **Happy** 😊 | Party Music | `"bollywood hindi party"` | Upbeat, energetic, celebratory songs |
| **Sad** 😔 | Romantic Music | `"bollywood hindi romantic"` | Emotional, romantic, sentimental songs |
| **Angry** 😡 | Energetic Rap | `"punjabi rap energetic"` | High-energy, intense, powerful tracks |
| **Neutral** 😐 | Chill/Lofi | `"hindi lofi chill"` | Calm, relaxed, ambient music |
| **Surprise** 😲 | Remix/Mashup | `"bollywood remix mashup"` | Fun, dynamic, remixed tracks |

---

## 📍 Code Location

**File**: `app/src/main/java/com/moodtune/app/music/services/AudiusApiService.java`

**Lines**: 191-201

```java
private List<String> buildSearchUrls(String host, String mood) throws Exception {
    String normalizedMood = mood == null ? "" : mood.toLowerCase(Locale.US);

    Map<String, String> moodQueries = new HashMap<>();
    moodQueries.put("happy", "bollywood hindi party");
    moodQueries.put("sad", "bollywood hindi romantic");
    moodQueries.put("angry", "punjabi rap energetic");
    moodQueries.put("neutral", "hindi lofi chill");
    moodQueries.put("surprise", "bollywood remix mashup");

    String query = moodQueries.getOrDefault(normalizedMood, "bollywood hindi mix");
    // ... rest of the code
}
```

---

## 🔍 How It Works

### Step-by-Step Process

1. **User Mood Detection**
   - User selects mood (Happy, Sad, Angry, Neutral, Surprise)
   - OR mood detected via Face Detection
   - OR mood detected via Chatbot

2. **Mood Normalization**
   ```java
   String normalizedMood = mood.toLowerCase(Locale.US);
   ```
   - Converts mood to lowercase for consistent matching

3. **Query Selection**
   ```java
   String query = moodQueries.getOrDefault(normalizedMood, "bollywood hindi mix");
   ```
   - Maps mood to specific search query
   - Default fallback: "bollywood hindi mix"

4. **API Search**
   - Searches Audius API with mood-specific query
   - Returns up to 40 tracks matching the query

5. **Fallback Strategy**
   - If mood-specific search fails, tries trending tracks:
     - Weekly trending (international genre)
     - Weekly trending (all genres)
     - Monthly trending (all genres)

---

## 📊 Detailed Mood Recommendations

### 1. **Happy Mood** → Party Music
- **Query**: `"bollywood hindi party"`
- **Rationale**: When you're happy, you want upbeat, celebratory music to enhance your positive mood
- **Music Type**: Energetic Bollywood party songs
- **Examples**: Dance tracks, celebration songs, upbeat Hindi music

### 2. **Sad Mood** → Romantic Music
- **Query**: `"bollywood hindi romantic"`
- **Rationale**: When you're sad, romantic/emotional music can be cathartic or comforting
- **Music Type**: Emotional Bollywood romantic songs
- **Examples**: Love ballads, emotional tracks, sentimental Hindi music
- **Note**: This could include both sad romantic songs and uplifting romantic songs

### 3. **Angry Mood** → Energetic Rap
- **Query**: `"punjabi rap energetic"`
- **Rationale**: When angry, high-energy music can help channel or release the energy
- **Music Type**: Intense Punjabi rap and energetic tracks
- **Examples**: Rap songs, high-energy Punjabi music, powerful tracks

### 4. **Neutral Mood** → Chill/Lofi
- **Query**: `"hindi lofi chill"`
- **Rationale**: When neutral, calm music maintains balance and relaxation
- **Music Type**: Relaxed, ambient Hindi lofi music
- **Examples**: Chill beats, lofi tracks, ambient Hindi music

### 5. **Surprise Mood** → Remix/Mashup
- **Query**: `"bollywood remix mashup"`
- **Rationale**: Surprise calls for dynamic, unexpected music combinations
- **Music Type**: Fun remixes and mashups
- **Examples**: Bollywood remixes, mashup tracks, dynamic remixed songs

---

## 🎯 Recommendation Strategy Analysis

### Current Approach: **Mood Matching**

**Pros:**
- ✅ Enhances current emotional state
- ✅ Provides music that resonates with how you feel
- ✅ Natural emotional alignment

**Cons:**
- ❌ Doesn't help change negative moods (sad/angry)
- ❌ May reinforce negative emotions
- ❌ No mood elevation strategy

### Alternative Approach: **Mood Elevation** (Not Currently Used)

If the app used **opposite mood strategy**:

| User Mood | Would Recommend | Purpose |
|-----------|----------------|---------|
| Sad | Happy/Upbeat Music | Lift mood |
| Angry | Calm/Relaxing Music | Reduce anger |
| Happy | Happy Music | Maintain happiness |

**Note**: The current app does **NOT** use this strategy.

---

## 🔄 Recommendation Flow Diagram

```
User Mood Detected
    ↓
Normalize Mood (toLowerCase)
    ↓
Lookup in moodQueries Map
    ↓
    ├─→ Happy → "bollywood hindi party"
    ├─→ Sad → "bollywood hindi romantic"
    ├─→ Angry → "punjabi rap energetic"
    ├─→ Neutral → "hindi lofi chill"
    ├─→ Surprise → "bollywood remix mashup"
    └─→ Unknown → "bollywood hindi mix" (default)
    ↓
Search Audius API with Query
    ↓
    ├─→ Success? → Return tracks
    │
    └─→ Failure? → Try trending tracks (fallback)
    ↓
Filter streamable tracks
    ↓
Shuffle and present to user
```

---

## 💡 Examples

### Example 1: User is Happy
```
User Mood: Happy 😊
    ↓
Search Query: "bollywood hindi party"
    ↓
Recommended: Upbeat party songs, dance tracks, celebration music
    ↓
Result: Music that matches and enhances happy mood
```

### Example 2: User is Sad
```
User Mood: Sad 😔
    ↓
Search Query: "bollywood hindi romantic"
    ↓
Recommended: Romantic/emotional songs, love ballads
    ↓
Result: Music that matches emotional state (not uplifting music)
```

### Example 3: User is Angry
```
User Mood: Angry 😡
    ↓
Search Query: "punjabi rap energetic"
    ↓
Recommended: High-energy rap, intense tracks
    ↓
Result: Music that matches the energy level (not calming music)
```

---

## 🛠️ How to Change Recommendation Strategy

If you want to implement **mood elevation** (opposite mood strategy), modify the `moodQueries` map:

### Current Code (Matching Mood):
```java
moodQueries.put("happy", "bollywood hindi party");
moodQueries.put("sad", "bollywood hindi romantic");
moodQueries.put("angry", "punjabi rap energetic");
```

### Alternative Code (Mood Elevation):
```java
// Lift sad mood with happy music
moodQueries.put("sad", "bollywood hindi party");

// Calm angry mood with chill music
moodQueries.put("angry", "hindi lofi chill");

// Maintain happy mood
moodQueries.put("happy", "bollywood hindi party");
```

---

## 📝 Summary

### **Current Behavior:**

1. **Happy User** → Gets **Happy/Party Music** ✅
2. **Sad User** → Gets **Romantic/Emotional Music** (matches mood, not uplifting)
3. **Angry User** → Gets **Energetic Rap** (matches energy, not calming)
4. **Neutral User** → Gets **Chill Music** ✅
5. **Surprise User** → Gets **Remix/Mashup** ✅

### **Key Point:**
The app **matches your mood** with similar music, it does **NOT** try to change your mood with opposite music.

---

## ❓ Frequently Asked Questions

### Q1: If I'm sad, will the app recommend happy music to cheer me up?
**A**: No. Currently, if you're sad, the app recommends "bollywood hindi romantic" music, which matches your emotional state rather than trying to elevate it.

### Q2: If I'm happy, what music will I get?
**A**: You'll get "bollywood hindi party" music - upbeat, celebratory songs that match and enhance your happy mood.

### Q3: Can I change the recommendation strategy?
**A**: Yes, you can modify the `moodQueries` map in `AudiusApiService.java` to implement mood elevation or any other strategy.

### Q4: What happens if the mood-specific search fails?
**A**: The app falls back to trending tracks (weekly/monthly trending) to ensure you always get music recommendations.

### Q5: Does the app consider user preferences?
**A**: The app also checks for user's previously saved playlists for the mood and mixes them with general recommendations (up to 5 user songs + general suggestions).

---

*Last Updated: Based on current codebase analysis*

