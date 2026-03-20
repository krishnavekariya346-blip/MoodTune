# Firebase Authentication Implementation Guide

## 🔐 Overview
This document explains how Firebase Authentication is implemented and used throughout the MoodTune Android application.

---

## 📋 Table of Contents
1. [Authentication Methods](#authentication-methods)
2. [Initialization](#initialization)
3. [Login Flow](#login-flow)
4. [Session Management](#session-management)
5. [User Profile Management](#user-profile-management)
6. [Sign Out](#sign-out)
7. [Authentication Checks](#authentication-checks)
8. [Code Examples](#code-examples)

---

## 🔑 Authentication Methods

### Supported Methods
1. **Google Sign-In** (Primary Method)
   - Uses Google Play Services Auth
   - OAuth 2.0 flow with Firebase
   - Single Sign-On (SSO) support

### Implementation Details
- **Library**: `com.google.firebase:firebase-auth:23.0.0`
- **Google Services**: `com.google.android.gms:play-services-auth:21.2.0`
- **Configuration**: Requires `google-services.json` and Web Client ID

---

## 🚀 Initialization

### 1. Firebase Auth Instance
```java
FirebaseAuth mAuth = FirebaseAuth.getInstance();
```
- **Location**: Used in all activities that require authentication
- **Purpose**: Get singleton instance of FirebaseAuth
- **Thread-safe**: Yes, can be called from any thread

### 2. Google Sign-In Configuration
```java
GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(getString(R.string.default_web_client_id))
    .requestEmail()
    .build();

GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
```
- **Location**: `LoginActivity.java` (lines 122-127)
- **Purpose**: Configure Google Sign-In with OAuth credentials
- **Required**: Web Client ID from Firebase Console

---

## 🔄 Login Flow

### Step-by-Step Process

#### 1. **Check Existing Session**
```java
FirebaseUser currentUser = mAuth.getCurrentUser();
if (currentUser != null) {
    // User already signed in, navigate to home
    navigateToHomePage();
    return;
}
```
- **Location**: `LoginActivity.java` (lines 48-54)
- **Purpose**: Skip login if user is already authenticated
- **Persistence**: Firebase Auth maintains session automatically

#### 2. **Check SharedPreferences**
```java
SharedPreferences prefs = getSharedPreferences("MoodTunePrefs", MODE_PRIVATE);
boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
```
- **Location**: `LoginActivity.java` (lines 57-62)
- **Purpose**: Additional persistence check (redundant but provides extra layer)

#### 3. **Initiate Google Sign-In**
```java
private void signIn() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    signInLauncher.launch(signInIntent);
}
```
- **Location**: `LoginActivity.java` (lines 138-152)
- **Purpose**: Launch Google Sign-In activity
- **Result**: Returns via ActivityResultLauncher

#### 4. **Handle Sign-In Result**
```java
signInLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        // Process result...
    }
);
```
- **Location**: `LoginActivity.java` (lines 65-118)
- **Purpose**: Handle Google Sign-In result asynchronously
- **Error Handling**: Comprehensive error handling for various failure scenarios

#### 5. **Firebase Authentication with Google**
```java
private void firebaseAuthWithGoogle(String idToken) {
    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
    mAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                // Handle success...
            }
        });
}
```
- **Location**: `LoginActivity.java` (lines 154-217)
- **Purpose**: Exchange Google ID token for Firebase Auth credential
- **Process**:
  1. Create `AuthCredential` from Google ID token
  2. Sign in to Firebase with credential
  3. Get authenticated `FirebaseUser`

#### 6. **Create/Update User Profile in Firestore**
```java
db.collection("users").document(user.getUid())
    .get()
    .addOnCompleteListener(userTask -> {
        if (document.exists()) {
            // Existing user - navigate to home
        } else {
            // New user - create profile
            UserModel userModel = new UserModel(user.getUid(), 
                                                user.getDisplayName(), 
                                                user.getEmail());
            db.collection("users").document(user.getUid())
                .set(userModel);
        }
    });
```
- **Location**: `LoginActivity.java` (lines 174-207)
- **Purpose**: Store user profile data in Firestore
- **Structure**: Creates user document with UID as document ID

---

## 💾 Session Management

### Automatic Session Persistence
- **Firebase Auth** automatically persists authentication state
- **No manual token management** required
- **Survives app restarts** - user remains logged in

### SharedPreferences Backup
```java
// Save login state
SharedPreferences prefs = getSharedPreferences("MoodTunePrefs", MODE_PRIVATE);
prefs.edit().putBoolean("isLoggedIn", true).apply();
```
- **Location**: `LoginActivity.java` (line 171)
- **Purpose**: Additional persistence layer (redundant but safe)

### Session Check Pattern
```java
FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
if (currentUser == null) {
    // Redirect to login or finish activity
    finish();
    return;
}
```
- **Used in**: All protected activities
- **Purpose**: Ensure user is authenticated before accessing features

---

## 👤 User Profile Management

### Getting Current User
```java
FirebaseAuth mAuth = FirebaseAuth.getInstance();
FirebaseUser currentUser = mAuth.getCurrentUser();

if (currentUser != null) {
    String userId = currentUser.getUid();
    String email = currentUser.getEmail();
    String displayName = currentUser.getDisplayName();
}
```

### User Information Available
- **UID**: Unique user identifier (`user.getUid()`)
- **Email**: User's email address (`user.getEmail()`)
- **Display Name**: User's name (`user.getDisplayName()`)
- **Photo URL**: Profile photo (if available)

### Profile Update (EditProfileActivity)
```java
// Update Firestore user document
Map<String, Object> updates = new HashMap<>();
updates.put("name", name);

db.collection("users").document(currentUser.getUid())
    .update(updates);
```
- **Location**: `EditProfileActivity.java` (lines 68-79)
- **Note**: Firebase Auth profile (email/displayName) is separate from Firestore profile

---

## 🔒 Password Management

### Change Password
```java
currentUser.updatePassword(newPassword)
    .addOnSuccessListener(aVoid -> {
        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
    })
    .addOnFailureListener(e -> {
        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    });
```
- **Location**: `ChangePasswordActivity.java` (lines 63-70)
- **Requirements**:
  - Minimum 6 characters
  - User must be recently authenticated
- **Note**: Only works for email/password accounts, not Google Sign-In

---

## 🚪 Sign Out

### Complete Sign-Out Process
```java
private void signOut() {
    // 1. Sign out from Firebase
    mAuth.signOut();
    
    // 2. Sign out from Google
    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
    googleSignInClient.signOut().addOnCompleteListener(task -> {
        // 3. Clear SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MoodTunePrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("isLoggedIn", false).apply();
        
        // 4. Navigate to login
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                       Intent.FLAG_ACTIVITY_NEW_TASK | 
                       Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    });
}
```
- **Location**: `SettingsActivity.java` (lines 100-118)
- **Steps**:
  1. Sign out from Firebase Auth
  2. Sign out from Google Sign-In
  3. Clear local preferences
  4. Navigate to login screen
  5. Clear activity stack

---

## ✅ Authentication Checks

### Activities with Auth Checks

#### 1. **HomePageActivity**
```java
mAuth = FirebaseAuth.getInstance();
currentUser = mAuth.getCurrentUser();

if (currentUser == null) {
    Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
    startActivity(intent);
    finish();
    return;
}
```
- **Location**: `HomePageActivity.java` (lines 59-69)
- **Action**: Redirects to login if not authenticated

#### 2. **ChatbotActivity**
```java
firebaseAuth = FirebaseAuth.getInstance();
FirebaseUser currentUser = firebaseAuth.getCurrentUser();
if (currentUser == null) {
    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
    finish();
    return;
}
```
- **Location**: `ChatbotActivity.java` (lines 56-62)
- **Action**: Shows toast and finishes activity

#### 3. **Reports Activities**
```java
FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
if (currentUser == null) {
    finish();
    return;
}
```
- **Locations**: 
  - `DailyReportActivity.java` (lines 47-51)
  - `WeeklyReportActivity.java` (lines 54-58)
  - `MonthlyReportActivity.java` (lines 55-59)
- **Action**: Silently finishes activity

#### 4. **Settings Activities**
```java
mAuth = FirebaseAuth.getInstance();
currentUser = mAuth.getCurrentUser();

if (currentUser == null) {
    finish();
    return;
}
```
- **Locations**: 
  - `SettingsActivity.java` (lines 35-41)
  - `EditProfileActivity.java` (lines 30-37)
  - `ChangePasswordActivity.java` (lines 25-31)
- **Action**: Finishes activity if not authenticated

#### 5. **Music Activities**
```java
currentUser = FirebaseAuth.getInstance().getCurrentUser();

if (currentUser == null) {
    finish();
    return;
}
```
- **Locations**: 
  - `HomeActivity.java` (line 99)
  - `MoodSelectionActivity.java` (line 41)
- **Action**: Finishes activity if not authenticated

---

## 📝 Code Examples

### Example 1: Complete Login Flow
```java
// 1. Initialize
FirebaseAuth mAuth = FirebaseAuth.getInstance();
GoogleSignInClient client = GoogleSignIn.getClient(this, gso);

// 2. Check existing session
FirebaseUser user = mAuth.getCurrentUser();
if (user != null) {
    // Already logged in
    return;
}

// 3. Start Google Sign-In
Intent signInIntent = client.getSignInIntent();
startActivityForResult(signInIntent, RC_SIGN_IN);

// 4. Handle result
GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
mAuth.signInWithCredential(credential);
```

### Example 2: Get User ID for Database Operations
```java
FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
if (currentUser != null) {
    String userId = currentUser.getUid();
    // Use userId for Firestore operations
    db.collection("moods").document(userId).get();
}
```

### Example 3: Check Authentication Before Operation
```java
private void saveMood(String mood) {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) {
        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        return;
    }
    
    String userId = user.getUid();
    // Proceed with save operation
}
```

---

## 🔍 Key Features

### 1. **Automatic Session Persistence**
- Firebase Auth automatically maintains session
- No manual token storage required
- Session survives app restarts

### 2. **Google Sign-In Integration**
- Seamless OAuth flow
- Single Sign-On (SSO) support
- No password required

### 3. **User Profile Synchronization**
- Automatic profile creation in Firestore
- User data linked via UID
- Profile updates in Firestore

### 4. **Error Handling**
- Comprehensive error handling for sign-in failures
- User-friendly error messages
- Graceful degradation

### 5. **Security**
- Secure token exchange
- OAuth 2.0 protocol
- Firebase-managed authentication

---

## 🛠️ Configuration Requirements

### 1. **Firebase Console Setup**
- Enable Google Sign-In provider
- Add SHA-1 fingerprint for debug/release
- Configure OAuth consent screen

### 2. **google-services.json**
- Place in `app/` directory
- Contains Firebase project configuration

### 3. **strings.xml**
```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
```
- Required for Google Sign-In
- Get from Firebase Console → Authentication → Sign-in method → Google

### 4. **AndroidManifest.xml**
- Internet permission (already present)
- Google Services plugin (already configured)

---

## 📊 Authentication State Flow

```
App Launch
    ↓
Check FirebaseAuth.getCurrentUser()
    ↓
    ├─→ User exists? → Yes → Navigate to HomePage
    │
    └─→ No → Show LoginActivity
            ↓
        User clicks "Sign in with Google"
            ↓
        Google Sign-In flow
            ↓
        Get ID Token
            ↓
        FirebaseAuth.signInWithCredential()
            ↓
        Check Firestore for user profile
            ↓
        ├─→ Exists? → Navigate to HomePage
        │
        └─→ New user? → Create profile → Navigate to HomePage
```

---

## ⚠️ Important Notes

1. **Google Sign-In Only**: The app currently only supports Google Sign-In, not email/password
2. **Password Change**: Only works for email/password accounts (not applicable for Google Sign-In users)
3. **Session Persistence**: Firebase handles this automatically - no manual token management needed
4. **User ID**: Always use `FirebaseUser.getUid()` for database operations, not email
5. **Error Handling**: Always check for null `FirebaseUser` before accessing user properties

---

## 🔗 Related Files

- **LoginActivity.java**: Main authentication logic
- **SettingsActivity.java**: Sign-out functionality
- **ChangePasswordActivity.java**: Password management (email/password only)
- **EditProfileActivity.java**: Profile updates
- **MoodDatabaseHandler.java**: Uses FirebaseUser for mood storage
- **All other activities**: Authentication checks

---

*Last Updated: Based on current codebase analysis*

