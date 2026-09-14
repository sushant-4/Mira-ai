# Mira

Mira is an Android application built with Kotlin and Jetpack Compose.

## Download the APK from GitHub

1. Open the repository on GitHub and select the **Actions** tab.
2. Open the latest successful **Build APK** workflow run.
3. Download the `mira-debug-apk` artifact from the **Artifacts** section.
4. Unzip the downloaded artifact and install `app-debug.apk` on an Android device.

The workflow runs automatically for pushes to `main` or `master`. To build it on demand, select **Build APK**, choose **Run workflow**, and open the resulting run.

This is a debug APK intended for testing and personal use. It is signed with Android's debug key and does not require a repository secret.