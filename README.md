# RKEA Tube — Android project

Project rebuilt from scratch based on the feature list and the Kotlin snippets shared in the conversation.

## Features in this starter build
- Arabic dark UI and bottom navigation.
- In-app WebView browser (starts at YouTube mobile website).
- DownloadManager-backed direct HTTP/HTTPS file downloads, handled by Android's download service and notification.
- Download status list.
- Local library of supported media files saved under the app's external Movies directory.
- Offline playback using AndroidX Media3/ExoPlayer, playback speed, ±10-second seek.
- Share downloaded media using FileProvider.

## Important limitations
- Web sites do not necessarily expose a downloadable direct media URL. This app does not bypass DRM or site protections, and does not extract protected streams from YouTube.
- The first build does not yet include the full advanced subtitle/gesture/lock UI, audio-only conversion, or an independent foreground download service. Direct MP3 files can be downloaded and played.
- Downloaded files are kept in app-specific external storage (`Android/data/com.rkea.tube/files/Movies/RKEA Tube`) and are visible in the in-app library. Android may remove them when the app is uninstalled.

## Build APK in GitHub Actions
The workflow `.github/workflows/android-apk.yml` builds a debug APK and uploads it as an artifact.
1. Create a GitHub repository and upload the **contents** of this project folder.
2. Open the repository's **Actions** tab.
3. Select **Build RKEA Tube APK** → **Run workflow**.
4. When it finishes green, open the run and download `RKEA-Tube-debug-apk`.
5. Extract `app-debug.apk` and install it on the phone. Android may ask to allow installation from that file manager/browser.

Build requirements: Android Gradle Plugin 8.7.3, Gradle 8.9, JDK 17, Android SDK platform 35.
