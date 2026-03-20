# Build and Run Instructions

## ✅ All Errors Fixed!

The following issues have been resolved:
1. ✅ Google Services plugin configuration
2. ✅ Missing MoodDotDecorator import
3. ✅ Missing View import in WeeklyReportActivity
4. ✅ Created app launcher icons (mipmap resources)

## How to Build and Run

### In Android Studio:

1. **Open the Project**
   - File → Open → Navigate to `MT` folder
   - Click OK

2. **Sync Gradle**
   - Android Studio will automatically sync
   - Wait for "Gradle sync finished" message
   - If errors appear, click "Sync Now"

3. **Start Emulator**
   - Tools → Device Manager
   - Click "Create Device" if needed
   - Select a device (e.g., Pixel 5)
   - Click the green Play button next to an emulator

4. **Run the App**
   - Click the green "Run" button (▶️) in toolbar
   - Or press `Shift + F10`
   - Select your emulator from the list
   - Wait for app to install and launch

### From Command Line:

```powershell
# Navigate to project
cd MT

# Build the APK
.\gradlew.bat assembleDebug

# Install on connected device/emulator
.\gradlew.bat installDebug

# Or use ADB directly
adb install app\build\outputs\apk\debug\app-debug.apk
```

## Expected Behavior

1. **App launches** → Login screen appears
2. **Click "Sign in with Google"** → Google Sign-In flow
3. **After login** → Home Page appears with:
   - Welcome message
   - Face Detection button
   - 5 mood buttons (😊 😔 😡 😐 😲)
   - Chatbot button at bottom
   - Sidebar menu (swipe from left)

## Troubleshooting

### Build Errors:
- **"Plugin not found"**: Make sure you've synced Gradle (File → Sync Project)
- **"Package not found"**: Clean and rebuild (Build → Clean Project, then Build → Rebuild Project)
- **"R class not found"**: Check that all resource files are valid XML

### Runtime Errors:
- **"Backend connection failed"**: Make sure your backend servers are running on `192.168.1.9:5000` (or `10.0.2.2:5000` for emulator)
- **"Firebase error"**: Check that `google-services.json` is in `app/` directory
- **"Login fails"**: Verify Firebase Authentication is enabled in Firebase Console

### Emulator Issues:
- **Slow performance**: Use x86_64 system image with hardware acceleration
- **Network issues**: Emulator uses `10.0.2.2` to access host machine's localhost
- **Camera not working**: Emulator camera can be enabled in AVD settings

## Backend Server Requirements

Make sure these are running:

1. **Face Detection Server**:
   - Endpoint: `/analyze`
   - Method: POST
   - Accepts: image/jpeg
   - Returns: `{"emotion": "happy", "confidence": 0.85}`

2. **Chatbot Server**:
   - Endpoint: `/predict`
   - Method: POST
   - Accepts: `{"text": "user message"}`
   - Returns: `{"mood": "happy", "confidence": "0.85"}`

Both servers should be accessible at:
- Real device: `http://192.168.1.9:5000`
- Emulator: `http://10.0.2.2:5000`

## Project Status

✅ All Java files created
✅ All layouts created
✅ All resources configured
✅ Build configuration fixed
✅ Google Services plugin configured
✅ All imports fixed
✅ Ready to build and run!

