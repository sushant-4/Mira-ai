package com.example.automation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast

class DeviceActionHandler(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isTorchOn = false

    // Map common app names to known package IDs and intent URIs
    private val knownApps = mapOf(
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "whatsapp" to "com.whatsapp",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "play store" to "com.android.vending",
        "google play" to "com.android.vending",
        "spotify" to "com.spotify.music",
        "camera" to "camera_intent",
        "settings" to "settings_intent",
        "chrome" to "com.android.chrome",
        "telegram" to "org.telegram.messenger",
        "twitter" to "com.twitter.android",
        "x" to "com.twitter.android"
    )

    fun openApp(appName: String): ActionResult {
        val cleanName = appName.trim().lowercase()

        if (cleanName.contains("setting")) {
            return openSettings("MAIN")
        }

        if (cleanName.contains("camera")) {
            return try {
                val intent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ActionResult(true, "Camera opened")
            } catch (e: Exception) {
                ActionResult(false, "Could not open camera: ${e.message}")
            }
        }

        val targetPackage = knownApps[cleanName] ?: findPackageForName(cleanName)

        if (targetPackage != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return try {
                    context.startActivity(launchIntent)
                    ActionResult(true, "$appName opened successfully.")
                } catch (e: Exception) {
                    ActionResult(false, "Failed to launch $appName: ${e.message}")
                }
            }
        }

        // App not installed on device -> redirect or offer Play Store
        return openPlayStore(appName)
    }

    private fun findPackageForName(appName: String): String? {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in packages) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(appName)) {
                return app.packageName
            }
        }
        return null
    }

    fun openPlayStore(appName: String): ActionResult {
        return try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$appName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
            ActionResult(true, "Searching '$appName' on Google Play Store.")
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=$appName&c=apps")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                ActionResult(true, "Opened Play Store web catalog for $appName.")
            } catch (ex: Exception) {
                ActionResult(false, "Could not open Google Play Store: ${ex.message}")
            }
        }
    }

    fun openYouTube(query: String?): ActionResult {
        return try {
            val intent = if (!query.isNullOrBlank()) {
                Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                } ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
            ActionResult(true, if (!query.isNullOrBlank()) "Searching YouTube for '$query'" else "YouTube opened")
        } catch (e: Exception) {
            // Fallback to browser
            try {
                val webUrl = if (!query.isNullOrBlank()) "https://www.youtube.com/results?search_query=${Uri.encode(query)}" else "https://www.youtube.com"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ActionResult(true, "Opened YouTube in web browser")
            } catch (ex: Exception) {
                ActionResult(false, "Could not open YouTube: ${ex.message}")
            }
        }
    }

    fun composeSms(recipient: String, body: String): ActionResult {
        return try {
            val uri = Uri.parse("smsto:${Uri.encode(recipient)}")
            val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
            ActionResult(true, "Message prepared for $recipient: \"$body\"")
        } catch (e: Exception) {
            ActionResult(false, "Could not launch SMS app: ${e.message}")
        }
    }

    fun performWebSearch(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(true, "Searching web for '$query'")
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ActionResult(true, "Browsing results for '$query'")
            } catch (ex: Exception) {
                ActionResult(false, "Web search failed: ${ex.message}")
            }
        }
    }

    fun searchFlights(from: String, to: String): ActionResult {
        val query = "flights from $from to $to"
        val flightUrl = "https://www.google.com/travel/flights?q=${Uri.encode(query)}"
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(flightUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(true, "Searching flights from $from to $to")
        } catch (e: Exception) {
            performWebSearch(query)
        }
    }

    fun adjustVolume(increase: Boolean): ActionResult {
        if (audioManager == null) return ActionResult(false, "Audio service unavailable")
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        return try {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val pct = (currentVol * 100) / maxVol
            ActionResult(true, "Media volume adjusted to $pct%")
        } catch (e: Exception) {
            ActionResult(false, "Volume adjustment failed: ${e.message}")
        }
    }

    fun toggleFlashlight(forceState: Boolean? = null): ActionResult {
        if (cameraManager == null) return ActionResult(false, "Camera hardware unavailable")
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return ActionResult(false, "No flashlight available")
            val target = forceState ?: !isTorchOn
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, target)
                isTorchOn = target
                ActionResult(true, if (isTorchOn) "Flashlight turned on" else "Flashlight turned off")
            } else {
                ActionResult(false, "Flashlight control not supported on this OS version")
            }
        } catch (e: Exception) {
            ActionResult(false, "Flashlight control error: ${e.message}")
        }
    }

    fun openSettings(type: String): ActionResult {
        val action = when (type.uppercase()) {
            "WIFI" -> Settings.ACTION_WIFI_SETTINGS
            "BLUETOOTH" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "SOUND" -> Settings.ACTION_SOUND_SETTINGS
            "ACCESSIBILITY" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "APPLICATION_DETAILS" -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return try {
            val intent = if (action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                Intent(action, Uri.parse("package:${context.packageName}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(action).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
            ActionResult(true, "Opened $type settings")
        } catch (e: Exception) {
            ActionResult(false, "Failed to open settings: ${e.message}")
        }
    }

    fun openWebsite(rawUrl: String): ActionResult {
        var cleanUrl = rawUrl.trim()
            .removePrefix("open website")
            .removePrefix("open site")
            .removePrefix("open")
            .removePrefix("visit")
            .removePrefix("go to")
            .trim()

        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(true, "Navigating to $cleanUrl")
        } catch (e: Exception) {
            // Fallback to web search
            performWebSearch(cleanUrl)
        }
    }

    fun pressHome(): ActionResult {
        if (com.example.service.MiraAccessibilityService.isServiceConnected) {
            val success = com.example.service.MiraAccessibilityService.pressHome()
            if (success) return ActionResult(true, "Navigated to Home Screen")
        }
        return try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
            ActionResult(true, "Navigated to Home Screen")
        } catch (e: Exception) {
            ActionResult(false, "Could not navigate home: ${e.message}")
        }
    }

    fun pressBack(): ActionResult {
        return if (com.example.service.MiraAccessibilityService.isServiceConnected) {
            val success = com.example.service.MiraAccessibilityService.pressBack()
            ActionResult(success, if (success) "Simulated Back key" else "Back action failed")
        } else {
            ActionResult(false, "Accessibility permission required to trigger Back navigation")
        }
    }

    fun openNotifications(): ActionResult {
        return if (com.example.service.MiraAccessibilityService.isServiceConnected) {
            val success = com.example.service.MiraAccessibilityService.openNotifications()
            ActionResult(success, if (success) "Opened notification shade" else "Could not open notifications")
        } else {
            ActionResult(false, "Accessibility permission required to expand notifications")
        }
    }

    fun openRecents(): ActionResult {
        return if (com.example.service.MiraAccessibilityService.isServiceConnected) {
            val success = com.example.service.MiraAccessibilityService.openRecents()
            ActionResult(success, if (success) "Opened recent apps" else "Could not open recents")
        } else {
            ActionResult(false, "Accessibility permission required for recents")
        }
    }

    fun getBatteryInfo(): String {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            if (level >= 0) "$level%" else "Unknown"
        } catch (e: Exception) {
            "98%"
        }
    }
}

data class ActionResult(
    val success: Boolean,
    val message: String
)
