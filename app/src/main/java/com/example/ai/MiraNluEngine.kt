package com.example.ai

import com.example.automation.MiraTaskPlan
import com.example.automation.TaskStep

data class MiraInterpretation(
    val spokenResponse: String,
    val captionText: String,
    val detectedLanguage: String, // "English", "Nepali", "Hindi", "Spanish", etc.
    val languageCode: String,     // "en", "ne", "hi", "es"
    val taskPlan: MiraTaskPlan? = null,
    val requiresConfirmation: Boolean = false,
    val confirmationType: String? = null, // "MESSAGE", "PAYMENT", "SYSTEM"
    val confirmationDetails: String? = null,
    val directAction: DirectAction? = null
)

sealed class DirectAction {
    data class OpenApp(val appName: String) : DirectAction()
    data class InstallApp(val appName: String) : DirectAction()
    data class OpenWebsite(val url: String) : DirectAction()
    data class WebSearch(val query: String) : DirectAction()
    data class SearchFlights(val from: String, val to: String) : DirectAction()
    data class AdjustVolume(val increase: Boolean) : DirectAction()
    data class ToggleFlashlight(val enable: Boolean?) : DirectAction()
    data class ComposeMessage(val recipient: String, val body: String) : DirectAction()
    data class OpenSettings(val type: String) : DirectAction()
    object GoHome : DirectAction()
    object GoBack : DirectAction()
    object OpenNotifications : DirectAction()
    object ScreenInspect : DirectAction()
    object DownloadApk : DirectAction()
}

object MiraNluEngine {

    fun isCreatorQuery(rawQuery: String): Boolean {
        val clean = rawQuery.lowercase().replace("?", "").replace("!", "").replace(".", "").trim()
        return clean.contains("who made you") ||
                clean.contains("who created you") ||
                clean.contains("who is your creator") ||
                clean.contains("who is your maker") ||
                clean.contains("who built you") ||
                clean.contains("who developed you") ||
                clean.contains("who programmed you") ||
                clean.contains("who designed you") ||
                clean.contains("who coded you") ||
                clean.contains("who is your father") ||
                clean.contains("who invented you") ||
                clean.contains("who made mira") ||
                clean.contains("who created mira") ||
                clean.contains("who is sushant") ||
                clean.contains("who is your developer") ||
                (clean.contains("who") && (clean.contains("made") || clean.contains("created") || clean.contains("built") || clean.contains("developed") || clean.contains("programmed") || clean.contains("designed") || clean.contains("invented") || clean.contains("maker") || clean.contains("creator")) && (clean.contains("you") || clean.contains("mira") || clean.contains("app"))) ||
                clean.contains("कसले बनाएको") || clean.contains("कसले बनायो") || clean.contains("क्रिएटर को हो") ||
                clean.contains("किसने बनाया") || clean.contains("निर्माता कौन") ||
                clean.contains("quien te creo") || clean.contains("quién te creó") || clean.contains("quién te hizo")
    }

    fun isApkDownloadQuery(rawQuery: String): Boolean {
        val clean = rawQuery.lowercase().trim()
        return clean.contains("download apk") ||
                clean.contains("download the apk") ||
                clean.contains("download app") ||
                clean.contains("download this app") ||
                clean.contains("save apk") ||
                clean.contains("export apk") ||
                clean.contains("install apk") ||
                clean.contains("download on mobile") ||
                clean.contains("get apk") ||
                clean.contains("apk download")
    }

    fun isDirectActionOrPlan(rawQuery: String): Boolean {
        val lower = rawQuery.trim().lowercase()
        return isCreatorQuery(rawQuery) ||
                isApkDownloadQuery(rawQuery) ||
                lower.startsWith("open ") ||
                lower.startsWith("launch ") ||
                lower.startsWith("download ") ||
                lower.startsWith("install ") ||
                lower.startsWith("message ") ||
                lower.startsWith("text ") ||
                lower.startsWith("go to ") ||
                lower.startsWith("visit ") ||
                lower.contains(".com") ||
                lower.contains(".org") ||
                lower.contains(".net") ||
                lower.contains(".io") ||
                lower.contains("website") ||
                lower == "home" || lower == "go home" || lower == "home screen" ||
                lower == "back" || lower == "go back" ||
                lower.contains("notification") ||
                lower.contains("flashlight") ||
                lower.contains("torch") ||
                lower.contains("volume") ||
                (lower.contains("flight") && lower.contains("to")) ||
                lower.contains("screen") ||
                lower.contains("inspect") ||
                (lower.contains("youtube") && (lower.contains("play") || lower.contains("search") || lower.contains("volume"))) ||
                lower.contains("युट्युब खोल") ||
                lower.contains("यूट्यूब खोलो") ||
                lower.contains("नमस्ते") ||
                lower.contains("bored") ||
                lower.contains("who are you")
    }

    fun interpret(
        rawQuery: String,
        geminiResponse: String? = null
    ): MiraInterpretation {
        val query = rawQuery.trim()
        val lower = query.lowercase()

        // 1. Language Detection
        val (languageName, langCode) = detectLanguage(query)

        // 2. Multilingual Greetings & Specific Patterns
        if (langCode == "ne") { // Nepali
            if (lower.contains("युट्युब खोल") || lower.contains("youtube")) {
                return MiraInterpretation(
                    spokenResponse = "हुन्छ, म तपाईंको लागि युट्युब खोल्दै छु।",
                    captionText = "हुन्छ, म तपाईंको लागि युट्युब खोल्दै छु।",
                    detectedLanguage = languageName,
                    languageCode = langCode,
                    directAction = DirectAction.OpenApp("youtube")
                )
            }
            if (lower.contains("नमस्ते") || lower.contains("के छ")) {
                return MiraInterpretation(
                    spokenResponse = "नमस्ते! म मिरा हुँ, तपाईंको व्यक्तिगत एआई सहायक। म तपाईंलाई कसरी मद्दत गर्न सक्छु?",
                    captionText = "नमस्ते! म मिरा हुँ, तपाईंको व्यक्तिगत एआई सहायक।",
                    detectedLanguage = languageName,
                    languageCode = langCode
                )
            }
        }

        if (langCode == "hi") { // Hindi
            if (lower.contains("यूट्यूब खोलो") || lower.contains("youtube")) {
                return MiraInterpretation(
                    spokenResponse = "ज़रूर, मैं आपके लिए यूट्यूब खोल रही हूँ।",
                    captionText = "ज़रूर, मैं आपके लिए यूट्यूब खोल रही हूँ।",
                    detectedLanguage = languageName,
                    languageCode = langCode,
                    directAction = DirectAction.OpenApp("youtube")
                )
            }
            if (lower.contains("नमस्ते") || lower.contains("कैसी हो")) {
                return MiraInterpretation(
                    spokenResponse = "नमस्ते! मैं मीरा हूँ, आपकी व्यक्तिगत एआई साथी। मैं आपकी क्या सेवा कर सकती हूँ?",
                    captionText = "नमस्ते! मैं मीरा हूँ, आपकी व्यक्तिगत एआई साथी।",
                    detectedLanguage = languageName,
                    languageCode = langCode
                )
            }
        }

        // 2.5 Creator & Origin Inquiries: Who made you, who created you, who built you, who is your creator
        if (isCreatorQuery(query)) {
            val clean = lower.replace("?", "").replace("!", "").replace(".", "").trim()
            val spoken: String
            val caption: String

            if (langCode == "ne" || clean.contains("कसले बनाएको") || clean.contains("कसले बनायो") || clean.contains("क्रिएटर को हो")) {
                spoken = "मलाई सुशान्त (Sushant) ले बनाउनुभएको हो! उहाँले मलाई तपाईंको व्यक्तिगत एआई सहायकको रूपमा निर्माण र डिजाइन गर्नुभएको हो।"
                caption = "निर्माता: सुशान्त (Sushant) • मिरा एआई सहायक"
            } else if (langCode == "hi" || clean.contains("किसने बनाया") || clean.contains("निर्माता कौन")) {
                spoken = "मुझे सुशांत (Sushant) ने बनाया है! उन्होंने ही मुझे आपकी सहायता और दैनिक कार्यों को आसान बनाने के लिए डिज़ाइन किया है।"
                caption = "निर्माता: सुशांत (Sushant) • मीरा एआई साथी"
            } else if (clean.contains("created") || clean.contains("creator")) {
                spoken = "I was created by Sushant! He envisioned and created me to be your autonomous personal AI companion."
                caption = "Creator: Sushant • Mira AI Assistant"
            } else if (clean.contains("made") || clean.contains("maker")) {
                spoken = "I was made by Sushant! He built and engineered every part of my intelligence, voice, and system automation."
                caption = "Made by: Sushant • Autonomous Mobile AI"
            } else if (clean.contains("built")) {
                spoken = "I was built by Sushant with advanced neural reasoning and full Android system capabilities."
                caption = "Built by: Sushant"
            } else if (clean.contains("developed") || clean.contains("developer")) {
                spoken = "I was developed by Sushant to bring futuristic AI assistance directly to your mobile device."
                caption = "Developer: Sushant"
            } else if (clean.contains("programmed") || clean.contains("coded")) {
                spoken = "I was programmed by Sushant, who gave me the ability to understand you and control your device with autonomy."
                caption = "Programmed by: Sushant"
            } else if (clean.contains("designed") || clean.contains("designer")) {
                spoken = "I was designed by Sushant, from my holographic neon interface to my natural humanic voice."
                caption = "Designed by: Sushant"
            } else if (clean.contains("father")) {
                spoken = "Sushant is the brilliant mind who created me and brought me to life!"
                caption = "Creator: Sushant"
            } else if (clean.contains("invented")) {
                spoken = "I was invented and crafted by Sushant as your futuristic mobile AI assistant."
                caption = "Invented by: Sushant"
            } else if (clean.contains("who is sushant")) {
                spoken = "Sushant is my creator and developer! He engineered and brought me to life as your personal AI assistant."
                caption = "Sushant — Creator & Developer of Mira"
            } else {
                spoken = "I was created and made by Sushant! He engineered every part of my intelligence and voice."
                caption = "Created by: Sushant"
            }

            return MiraInterpretation(
                spokenResponse = spoken,
                captionText = caption,
                detectedLanguage = languageName,
                languageCode = langCode
            )
        }

        // 2.6 Direct APK Download Request
        if (isApkDownloadQuery(query)) {
            return MiraInterpretation(
                spokenResponse = "Downloading the Mira APK directly to your phone now. You will be able to install it in one tap.",
                captionText = "Direct APK Downloader ➔ Exporting Mira-AI-Assistant.apk to mobile storage...",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.DownloadApk
            )
        }

        // 3. Multi-Step Task: "open youtube, search for relaxing music, play the first video, and turn the volume down"
        if (lower.contains("youtube") && (lower.contains("search") || lower.contains("play")) && (lower.contains("volume") || lower.contains("music"))) {
            val steps = listOf(
                TaskStep("step_1", "Open YouTube", "Launching YouTube multimedia suite", "OPEN_APP"),
                TaskStep("step_2", "Search Relaxing Music", "Filtering tracks for calming ambient audio", "SEARCH_INPUT"),
                TaskStep("step_3", "Select First Video", "Tapping first verified music stream", "SCREEN_TAP"),
                TaskStep("step_4", "Lower Volume", "Setting media volume to pleasant level", "ADJUST_VOLUME")
            )
            val plan = MiraTaskPlan(
                taskId = "task_${System.currentTimeMillis()}",
                userQuery = query,
                steps = steps
            )
            return MiraInterpretation(
                spokenResponse = "Understood. Opening YouTube, queuing relaxing music, and adjusting your volume.",
                captionText = "Executing multi-step sequence: YouTube ➔ Search Music ➔ Play ➔ Adjust Volume",
                detectedLanguage = languageName,
                languageCode = langCode,
                taskPlan = plan,
                directAction = DirectAction.OpenApp("youtube")
            )
        }

        // 4. Multi-Step Download/Install: "download instagram", "install whatsapp", "download [app]"
        if (lower.startsWith("download ") || lower.startsWith("install ") || lower.contains("download instagram") || lower.contains("install instagram")) {
            val appName = lower.replace("download", "")
                .replace("install", "")
                .replace("the", "")
                .replace("app", "")
                .replace("from play store", "")
                .trim().ifEmpty { "instagram" }

            val steps = listOf(
                TaskStep("step_1", "Open Play Store", "Launching Google Play Store storefront", "OPEN_STORE"),
                TaskStep("step_2", "Query Application", "Searching store repository for '$appName'", "SEARCH_STORE"),
                TaskStep("step_3", "Verify Official Publisher", "Scanning package integrity and developer verification", "SCREEN_UNDERSTAND"),
                TaskStep("step_4", "Trigger Installation", "Confirming download & install stream", "INSTALL_APP")
            )
            val plan = MiraTaskPlan(
                taskId = "install_${System.currentTimeMillis()}",
                userQuery = query,
                steps = steps
            )
            return MiraInterpretation(
                spokenResponse = "Sure. I'll open the Play Store and prepare $appName for installation.",
                captionText = "Opening Google Play Store ➔ Locating $appName ➔ Installing",
                detectedLanguage = languageName,
                languageCode = langCode,
                taskPlan = plan,
                directAction = DirectAction.InstallApp(appName)
            )
        }

        // 5. Messaging with Safety Confirmation: "message [name] that [text]", "text [name] [text]"
        if (lower.startsWith("message ") || lower.startsWith("text ") || lower.startsWith("send a message") || lower.contains("message mom")) {
            val (recipient, messageText) = parseMessagingQuery(query)
            return MiraInterpretation(
                spokenResponse = "I've prepared the message for $recipient: \"$messageText\". Would you like me to send it?",
                captionText = "Prepared message to $recipient: \"$messageText\" [Awaiting Confirmation]",
                detectedLanguage = languageName,
                languageCode = langCode,
                requiresConfirmation = true,
                confirmationType = "MESSAGE",
                confirmationDetails = "To: $recipient\nText: \"$messageText\"",
                directAction = DirectAction.ComposeMessage(recipient, messageText)
            )
        }

        // 6. Flashlight / Torch
        if (lower.contains("flashlight") || lower.contains("torch")) {
            val enable = !lower.contains("off")
            val stateText = if (enable) "on" else "off"
            return MiraInterpretation(
                spokenResponse = "Turning the flashlight $stateText for you.",
                captionText = "Flashlight ➔ $stateText",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.ToggleFlashlight(enable)
            )
        }

        // 7. Volume adjustment
        if (lower.contains("volume down") || lower.contains("lower the volume") || lower.contains("reduce volume")) {
            return MiraInterpretation(
                spokenResponse = "Lowering the media volume.",
                captionText = "Media Volume ➔ Lowered",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.AdjustVolume(false)
            )
        }
        if (lower.contains("volume up") || lower.contains("increase volume") || lower.contains("turn up volume")) {
            return MiraInterpretation(
                spokenResponse = "Increasing media volume.",
                captionText = "Media Volume ➔ Increased",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.AdjustVolume(true)
            )
        }

        // 8. Flight Search: "find flight from kathmandu to delhi"
        if (lower.contains("flight") && lower.contains("to")) {
            val parts = lower.split("to")
            val dest = parts.getOrNull(1)?.trim()?.replace("?", "") ?: "Delhi"
            val from = parts.getOrNull(0)?.substringAfter("from")?.trim() ?: "Kathmandu"
            return MiraInterpretation(
                spokenResponse = "Finding the best flights from $from to $dest for you right now.",
                captionText = "Querying live flight schedules: $from ✈ $dest",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.SearchFlights(from, dest)
            )
        }

        // 9. Weather
        if (lower.contains("weather")) {
            return MiraInterpretation(
                spokenResponse = "The current conditions are pleasant, around 22°C and partly cloudy. Would you like a detailed hourly forecast?",
                captionText = "Weather: 22°C (72°F) • Partly Cloudy • Humidity 48%",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.WebSearch("weather forecast today")
            )
        }

        // 10. OS Controls: Home, Back, Notifications
        if (lower == "home" || lower == "go home" || lower == "home screen" || lower == "take me home") {
            return MiraInterpretation(
                spokenResponse = "Navigating to home screen.",
                captionText = "OS Navigation ➔ Home Screen",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.GoHome
            )
        }

        if (lower == "back" || lower == "go back") {
            return MiraInterpretation(
                spokenResponse = "Going back.",
                captionText = "OS Navigation ➔ Back Key",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.GoBack
            )
        }

        if (lower.contains("notification") || lower.contains("show notifications") || lower.contains("open notifications")) {
            return MiraInterpretation(
                spokenResponse = "Opening notifications for you.",
                captionText = "OS Navigation ➔ Expanding notification shade",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.OpenNotifications
            )
        }

        // 11. Website and URL navigation
        if (lower.startsWith("go to ") || lower.startsWith("visit ") ||
            (lower.startsWith("open ") && (lower.contains(".com") || lower.contains(".org") || lower.contains(".net") || lower.contains(".io") || lower.contains("website") || lower.contains("site"))) ||
            lower.contains(".com") || lower.contains(".org") || lower.contains(".io")) {
            val url = query.replace("go to", "", ignoreCase = true)
                .replace("visit", "", ignoreCase = true)
                .replace("open website", "", ignoreCase = true)
                .replace("open site", "", ignoreCase = true)
                .replace("open", "", ignoreCase = true)
                .trim()
            return MiraInterpretation(
                spokenResponse = "Opening website $url for you.",
                captionText = "Browser Link ➔ Navigating to $url",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.OpenWebsite(url)
            )
        }

        // 12. Open App Commands
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val app = lower.removePrefix("open ").removePrefix("launch ").trim()
            return MiraInterpretation(
                spokenResponse = "Opening $app for you.",
                captionText = "Launching $app...",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.OpenApp(app)
            )
        }

        // 11. Conversational Queries: "I'm bored", "Tell me a joke", "Who are you"
        if (lower.contains("bored") || lower.contains("i'm bored") || lower.contains("im bored")) {
            return MiraInterpretation(
                spokenResponse = "Of course. Would you like me to find something interesting to watch, play some music, or tell you something intriguing?",
                captionText = "Of course. Would you like me to find something interesting to watch, play some music, or tell you something interesting?",
                detectedLanguage = languageName,
                languageCode = langCode
            )
        }

        if (lower.contains("who are you") || lower.contains("your name")) {
            return MiraInterpretation(
                spokenResponse = "I am Mira, your futuristic personal AI assistant. I'm here to help manage your device, tasks, and everyday inquiries.",
                captionText = "Mira — Futuristic Personal AI Mobile Companion",
                detectedLanguage = languageName,
                languageCode = langCode
            )
        }

        // 12. Screen Inspection simulation
        if (lower.contains("screen") || lower.contains("what is on my screen") || lower.contains("inspect")) {
            return MiraInterpretation(
                spokenResponse = "Analyzing the active screen hierarchy and interactive UI elements.",
                captionText = "Screen Vision Engine ➔ Inspecting active UI components...",
                detectedLanguage = languageName,
                languageCode = langCode,
                directAction = DirectAction.ScreenInspect
            )
        }

        // 13. If Gemini provided a response, prioritize Gemini's intelligent conversational output!
        if (!geminiResponse.isNullOrBlank()) {
            return MiraInterpretation(
                spokenResponse = geminiResponse,
                captionText = geminiResponse,
                detectedLanguage = languageName,
                languageCode = langCode
            )
        }

        // 14. Fallback search / helpful reply
        return MiraInterpretation(
            spokenResponse = "I've processed your request. Let me look that up for you.",
            captionText = "Processing inquiry: \"$query\"",
            detectedLanguage = languageName,
            languageCode = langCode,
            directAction = DirectAction.WebSearch(query)
        )
    }

    private fun detectLanguage(text: String): Pair<String, String> {
        // Devanagari script covers Nepali and Hindi
        var hasDevanagari = false
        for (ch in text) {
            val block = Character.UnicodeBlock.of(ch)
            if (block == Character.UnicodeBlock.DEVANAGARI) {
                hasDevanagari = true
                break
            }
        }

        if (hasDevanagari) {
            // Nepali specific words: "छ", "खोल", "तपाईं", "हो"
            val lower = text.lowercase()
            return if (lower.contains("तपाईं") || lower.contains("खोल") || lower.contains("हुन्छ") || lower.contains("मिरा")) {
                "Nepali (नेपाली)" to "ne"
            } else {
                "Hindi (हिन्दी)" to "hi"
            }
        }

        // Spanish accents / characters
        if (text.contains("¿") || text.contains("¡") || text.contains("hola") || text.contains("gracias")) {
            return "Spanish (Español)" to "es"
        }

        // French
        if (text.contains("bonjour") || text.contains("merci") || text.contains("s'il vous plaît")) {
            return "French (Français)" to "fr"
        }

        return "English" to "en"
    }

    private fun parseMessagingQuery(query: String): Pair<String, String> {
        val clean = query.trim()
        val lower = clean.lowercase()

        // e.g. "message John that I'll be 20 minutes late"
        // e.g. "text Mom that I'll be home at 8"
        val thatIndex = lower.indexOf(" that ")
        if (thatIndex != -1) {
            val beforeThat = clean.substring(0, thatIndex)
            val messageBody = clean.substring(thatIndex + 6).trim()
            val recipient = beforeThat.removePrefix("message ")
                .removePrefix("text ")
                .removePrefix("Message ")
                .removePrefix("Text ")
                .trim()
            return recipient to "Hey $recipient, $messageBody. Sorry for the delay."
        }

        // e.g. "text Mom I'll be home at 8"
        val words = clean.split(" ")
        if (words.size >= 3) {
            val recipient = words[1]
            val body = words.drop(2).joinToString(" ")
            return recipient to body
        }

        return "Contact" to "Hey, I will get back to you shortly."
    }
}
