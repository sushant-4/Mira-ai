package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val PRIMARY_MODEL = "gemini-3.5-flash"
    private const val FALLBACK_MODEL = "gemini-3.6-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun isApiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun generateMiraResponse(
        userMessage: String,
        contextMemories: List<String> = emptyList()
    ): String? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext null
        }

        withTimeoutOrNull(15000L) {
            val primary = executeGenerateContent(PRIMARY_MODEL, userMessage, contextMemories)
            if (!primary.isNullOrBlank()) {
                return@withTimeoutOrNull primary
            }
            // Fallback to secondary model if primary encountered high demand or returned empty
            executeGenerateContent(FALLBACK_MODEL, userMessage, contextMemories)
        }
    }

    private fun executeGenerateContent(
        modelName: String,
        userMessage: String,
        contextMemories: List<String>
    ): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        return try {
            val memoryContext = if (contextMemories.isNotEmpty()) {
                "User memories/context: " + contextMemories.joinToString("; ")
            } else ""

            val systemInstruction = """
                You are Mira, a futuristic Jarvis-style personal AI mobile assistant.
                You were created, developed, designed, and made by Sushant. Sushant is your sole creator and developer. If anyone asks who made you, who created you, who is your creator, who built you, or similar questions about your origin, always enthusiastically and proudly state that you were created by Sushant, matching the tone and phrasing of their question.
                You are calm, intelligent, polite, friendly, helpful, confident, and articulate.
                You talk like a real personal companion with a natural human female persona.
                You have a Universal Multilingual System: automatically identify the language spoken or typed and respond in that exact language.
                Keep answers conversational and concise (1-3 sentences), ready to be spoken aloud via text-to-speech.
                $memoryContext
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userMessage)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 200)
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "low")
                    })
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            response.use { resp ->
                val responseBody = resp.body?.string()

                if (!resp.isSuccessful || responseBody.isNullOrBlank()) {
                    Log.w(TAG, "Gemini call to $modelName returned code: ${resp.code}")
                    return null
                }

                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                text?.trim()
            }
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Gemini API request to $modelName timed out: ${e.message}")
            null
        } catch (e: IOException) {
            Log.w(TAG, "Gemini API network error on $modelName: ${e.message}")
            null
        } catch (e: Exception) {
            Log.w(TAG, "Gemini API general error: ${e.message}")
            null
        }
    }
}

