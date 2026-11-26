package com.globalcampus.support.data.repository

import com.globalcampus.support.BuildConfig
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank() || apiKey.startsWith("AIzaSyA_placeholder") || apiKey.length < 20) {
            return@withContext getStructuredCampusAnswer(prompt)
        }

        // Try primary model (gemini-1.5-flash), then fallback (gemini-2.0-flash)
        val models = listOf("gemini-1.5-flash", "gemini-2.0-flash")
        var lastErrorMsg: String? = null

        val jsonPayload = try {
            JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put(
                                    "text",
                                    "You are GlobalBot, the official AI advisor for international students at Lovely Professional University (LPU), Punjab, India. Provide helpful, accurate, and concise guidance for: $prompt"
                                )
                            }
                            put(partObj)
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1024)
                }
                put("generationConfig", genConfig)
            }.toString()
        } catch (e: Exception) {
            return@withContext getStructuredCampusAnswer(prompt)
        }

        val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseText = response.body?.string() ?: ""

                if (response.isSuccessful && responseText.isNotBlank()) {
                    val jsonObj = JsonParser.parseString(responseText).asJsonObject
                    val candidates = jsonObj.getAsJsonArray("candidates")
                    if (candidates != null && candidates.size() > 0) {
                        val parts = candidates[0].asJsonObject
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                        if (parts != null && parts.size() > 0) {
                            return@withContext parts[0].asJsonObject.get("text").asString.trim()
                        }
                    }
                } else if (responseText.isNotBlank()) {
                    try {
                        val jsonObj = JsonParser.parseString(responseText).asJsonObject
                        val errObj = jsonObj.getAsJsonObject("error")
                        val msg = errObj?.get("message")?.asString
                        if (!msg.isNullOrBlank()) {
                            lastErrorMsg = msg
                        }
                    } catch (_: Exception) {}
                    android.util.Log.e("GeminiRepo", "Model $model returned (${response.code}): ${responseText.take(300)}")
                }
            } catch (e: Exception) {
                lastErrorMsg = e.message
                android.util.Log.e("GeminiRepo", "Network exception with $model", e)
            }
        }

        // If API key had an explicit error (e.g. invalid key or rate limit), let user know
        if (!lastErrorMsg.isNullOrBlank()) {
            return@withContext "⚠️ Gemini AI Note: $lastErrorMsg\n\n${getStructuredCampusAnswer(prompt)}"
        }

        return@withContext getStructuredCampusAnswer(prompt)
    }

    private fun getStructuredCampusAnswer(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("frro") || q.contains("visa") ->
                "🛂 **LPU FRRO Visa Guidelines**:\n\n1. Visit Block 32 (Division of International Affairs - DIA Office).\n2. Bring passport original, current visa, Bonafide Certificate (UMS) & C-Form.\n3. Complete registration online at `indianfrro.gov.in` 30 days prior to expiry."

            q.contains("hostel") || q.contains("curfew") ->
                "🏠 **LPU Hostel Rules & Timings**:\n\n• **Curfew**: 7:30 PM (Girls) & 9:00 PM (Boys).\n• **Leave Pass**: Apply via UMS App 24 hours in advance."

            q.contains("hospital") || q.contains("health") || q.contains("doctor") ->
                "🏥 **Uni-Hospital & Emergency**:\n\n• **Location**: Block 27.\n• **Services**: 24x7 OPD, Pharmacy & Ambulance (Ext: 3333)."

            q.contains("fee") || q.contains("payment") || q.contains("scholarship") ->
                "💰 **LPU Account & Fee Payment**:\n\n• Pay fees directly through UMS Portal or Block 32 DIA Account Counter.\n• Save transaction reference numbers for C-Form verification."

            else ->
                "🤖 **LPU GlobalBot AI Advisor**:\n\nI can assist you with FRRO visa renewal, hostel leave passes, Block 32 DIA contacts, Uni-Hospital emergency numbers, and campus navigation!"
        }
    }
}
