# Mira

Mira is an Android application built with Kotlin and Jetpack Compose.

## Download the APK from GitHub

Open the latest release on an Android phone and download the APK directly:

https://github.com/sushant-4/Mira-ai/releases/latest/download/Mira-AI-Assistant.apk

The workflow also keeps a `mira-debug-apk` Actions artifact as a fallback for 30 days.

The workflow publishes a new release automatically for pushes to `main` or `master`. To build it
on demand, select **Build APK**, choose **Run workflow**, and open the workflow summary for the
direct download URL.

This is a debug APK intended for testing and personal use. It is signed with Android's debug key and does not require a repository secret.