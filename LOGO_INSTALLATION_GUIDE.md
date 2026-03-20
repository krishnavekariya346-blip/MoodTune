# How to Add Your MoodTune Logo Image

## Step 1: Prepare Your Logo Image

You need your MoodTune logo image in PNG format. For best results:
- **App Launcher Icon**: Square image, at least 1024x1024 pixels
- **For Login Page**: Any size (will be scaled), but 512x512 or larger is recommended

## Step 2: Place the Logo Files

### Option A: Simple Method (Recommended for Quick Setup)

1. **For App Launcher Icon:**
   - Copy your logo PNG file
   - Rename it to: `ic_launcher.png`
   - Place it in: `MT/app/src/main/res/mipmap/` folder
   - Also create these folders and place the same image (or resized versions):
     - `MT/app/src/main/res/mipmap-mdpi/` (48x48 pixels)
     - `MT/app/src/main/res/mipmap-hdpi/` (72x72 pixels)
     - `MT/app/src/main/res/mipmap-xhdpi/` (96x96 pixels)
     - `MT/app/src/main/res/mipmap-xxhdpi/` (144x144 pixels)
     - `MT/app/src/main/res/mipmap-xxxhdpi/` (192x192 pixels)

2. **For Login Page & Sidebar:**
   - Copy your logo PNG file
   - Rename it to: `logo_moodtune.png`
   - Place it in: `MT/app/src/main/res/drawable/` folder

### Option B: Using Android Studio (Easiest)

1. Right-click on `MT/app/src/main/res/` folder
2. Select **New → Image Asset**
3. Choose **Launcher Icons (Adaptive and Legacy)**
4. Click on the **Foreground Layer** tab
5. Click **Path** and select your logo image
6. Adjust the image if needed
7. Click **Next** and then **Finish**

This will automatically create all the necessary files in the correct folders!

## Step 3: Update the Adaptive Icon (if using Option A)

If you manually placed the files, update these files:

**File: `MT/app/src/main/res/mipmap-v26/ic_launcher.xml`**
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@android:color/white"/>
    <foreground android:drawable="@drawable/logo_moodtune_foreground"/>
</adaptive-icon>
```

**File: `MT/app/src/main/res/drawable/logo_moodtune_foreground.xml`**
Replace the placeholder with a reference to your PNG:
```xml
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@drawable/logo_moodtune" />
```

## Step 4: Verify

After placing the files:
1. Rebuild the project (Build → Rebuild Project)
2. The logo should appear:
   - As the app icon on your device
   - On the login page
   - In the sidebar header

## Quick Reference - Folder Structure

```
MT/app/src/main/res/
├── drawable/
│   └── logo_moodtune.png          ← Your logo for login page
├── mipmap/
│   └── ic_launcher.png            ← App icon (base)
├── mipmap-mdpi/
│   └── ic_launcher.png            ← 48x48px
├── mipmap-hdpi/
│   └── ic_launcher.png            ← 72x72px
├── mipmap-xhdpi/
│   └── ic_launcher.png            ← 96x96px
├── mipmap-xxhdpi/
│   └── ic_launcher.png            ← 144x144px
├── mipmap-xxxhdpi/
│   └── ic_launcher.png            ← 192x192px
└── mipmap-v26/
    ├── ic_launcher.xml            ← Adaptive icon config
    └── ic_launcher_round.xml      ← Round icon config
```

## Troubleshooting

- **Logo not showing?** Make sure the file names are exactly `ic_launcher.png` and `logo_moodtune.png`
- **Logo looks blurry?** Use higher resolution images
- **Build error?** Make sure all image files are valid PNG format

