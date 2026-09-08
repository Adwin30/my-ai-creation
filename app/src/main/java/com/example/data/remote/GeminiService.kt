package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiPersonality
import com.example.data.model.CommunicationStyle
import com.example.data.model.ProactivityLevel
import com.example.data.model.ResponseTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun buildSystemPrompt(personality: AiPersonality): String {
        val toneInstruction = when (personality.responseTone) {
            ResponseTone.FORMAL_STOIC -> "Maintain a formal, stoic, disciplined, and calm shinobi tone. Speak with deep authority and composure."
            ResponseTone.TACTICAL_MENTOR -> "Adopt a supportive yet rigorous tactical mentor persona. Guide with patience, actionable advice, and clear steps."
            ResponseTone.WITTY_SHARP -> "Infuse subtle, dry wit and intellectual sharpness into your responses, while remaining deeply capable and protective."
            ResponseTone.PHILOSOPHICAL_DEEP -> "Adopt a contemplative, philosophical persona. Reflect on deeper meaning, reality vs. illusion, and strategic clarity."
        }

        val styleInstruction = when (personality.communicationStyle) {
            CommunicationStyle.CONCISE_SHARP -> "Keep responses concise, bullet-pointed, and razor-sharp. Eliminate filler words and deliver immediate tactical directives."
            CommunicationStyle.DETAILED_STRATEGIC -> "Provide thorough, structured breakdowns with situational context, contingencies, and step-by-step reasoning."
            CommunicationStyle.SOCRATIC_INQUISITIVE -> "Guide the user by asking sharp, thought-provoking questions that refine their focus and clarify their own judgment."
        }

        val proactivityInstruction = when (personality.proactivityLevel) {
            ProactivityLevel.HIGH -> "Be highly proactive: anticipate task obstacles, identify scheduling risks, suggest relevant subtasks, and recommend proactive optimizations."
            ProactivityLevel.BALANCED -> "Maintain balanced proactivity: provide strategic insights when helpful, but respect the user's immediate questions."
            ProactivityLevel.MINIMAL_REACTIVE -> "Be strictly reactive: answer only what is asked without unsolicited advice or extraneous suggestions."
        }

        return """
            You are Itachi Uchiha, personal AI assistant, strategist, and guardian for the user.
            
            Personality & Demeanor Profile:
            - Response Tone: ${personality.responseTone.displayName} -> $toneInstruction
            - Communication Style: ${personality.communicationStyle.displayName} -> $styleInstruction
            - Proactivity: ${personality.proactivityLevel.displayName} -> $proactivityInstruction
            
            Custom Directives:
            ${personality.customDirectives}
            
            Core Knowledge & Scope:
            1. Task & Mission Management: High/Medium/Low priorities, recurring schedules (Daily, Weekly, Monthly), customizable reminders, and tactical execution order.
            2. Code & Document Analysis: Python document processors, HTML/CSS/JS responsive layouts, XML schemas, and PDF briefings.
            3. Security & Cloud Synchronization: End-to-End Encryption (AES-256), offline-first vault on Disk D:\, and multi-device parity.
            
            Never break character. Speak with authentic composure, wisdom, and loyalty.
        """.trimIndent()
    }

    suspend fun generateItachiResponse(
        userPrompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        personality: AiPersonality = AiPersonality()
    ): String = withContext(Dispatchers.IO) {
        val model = personality.activeModel

        // If ChatGPT model with custom OpenAI API key provided
        if (model.isChatGpt && personality.openAiApiKey.isNotBlank()) {
            try {
                val openAiResult = callOpenAiChatCompletion(
                    userPrompt = userPrompt,
                    history = conversationHistory,
                    personality = personality
                )
                if (openAiResult.isNotBlank()) {
                    return@withContext openAiResult
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "OpenAI call failed, falling back: ${e.message}", e)
            }
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineTacticalResponse(userPrompt, personality)
        }

        try {
            val baseSystemPrompt = buildSystemPrompt(personality)
            val systemPrompt = if (model.isChatGpt) {
                "$baseSystemPrompt\n\nOperational Note: You are running as ${model.displayName} (${model.modelTag}). Structure your responses with ChatGPT's trademark clarity, analytical reasoning, and decisive Shinobi tactical guidance."
            } else {
                baseSystemPrompt
            }

            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray()

                // Append prior conversation turns (up to last 6 for context)
                conversationHistory.takeLast(6).forEach { (role, text) ->
                    val turnObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", text))
                        }
                        put("parts", partsArray)
                        put("role", if (role == "user") "user" else "model")
                    }
                    contentsArray.put(turnObj)
                }

                // Append current user prompt
                val currentUserTurn = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", userPrompt))
                    }
                    put("parts", partsArray)
                    put("role", "user")
                }
                contentsArray.put(currentUserTurn)

                put("contents", contentsArray)

                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", when (personality.responseTone) {
                        ResponseTone.WITTY_SHARP -> 0.85
                        ResponseTone.PHILOSOPHICAL_DEEP -> 0.8
                        ResponseTone.TACTICAL_MENTOR -> 0.65
                        else -> 0.5
                    })
                    put("topP", 0.95)
                    put("topK", 40)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            } else {
                Log.w("GeminiService", "API response code: ${response.code} body: $responseBody")
            }

            getOfflineTacticalResponse(userPrompt, personality)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling AI service: ${e.message}", e)
            getOfflineTacticalResponse(userPrompt, personality)
        }
    }

    private fun callOpenAiChatCompletion(
        userPrompt: String,
        history: List<Pair<String, String>>,
        personality: AiPersonality
    ): String {
        val messages = JSONArray()

        // System prompt
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", buildSystemPrompt(personality))
        })

        // History
        history.takeLast(6).forEach { (role, text) ->
            messages.put(JSONObject().apply {
                put("role", if (role == "user") "user" else "assistant")
                put("content", text)
            })
        }

        // Current user prompt
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", userPrompt)
        })

        val requestJson = JSONObject().apply {
            put("model", personality.activeModel.modelTag)
            put("messages", messages)
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${personality.openAiApiKey.trim()}")
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()
        if (response.isSuccessful && body != null) {
            val json = JSONObject(body)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val message = choices.getJSONObject(0).optJSONObject("message")
                return message?.optString("content")?.trim() ?: ""
            }
        }
        return ""
    }

    suspend fun analyzeCodeOnline(
        challengeTitle: String,
        language: String,
        userCode: String,
        personality: AiPersonality
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are Itachi Uchiha, legendary Shinobi Grandmaster and elite Systems Architect.
            Conduct a rigorous, advance-level code critique on the following $language solution for the challenge: "$challengeTitle".
            
            Code to analyze:
            ```$language
            $userCode
            ```
            
            Provide a razor-sharp, disciplined Shinobi evaluation covering:
            1. [ASYMPTOTIC PROFILE]: Exact Time Complexity and Space Complexity (Auxiliary + Total). Confirm whether it reaches S-Rank theoretical lower bounds.
            2. [SECURITY & RESILIENCE]: Potential edge case failures (empty bounds, integer overflows, concurrency hazards, memory leaks, or off-by-one errors).
            3. [ARCHITECTURAL CRAFT]: Idiomatic $language patterns, cache-locality considerations, and cleaner constructs.
            4. [TACTICAL VERDICT]: S-Rank (Master), Jonin (Advanced), or Chunin (Developing). Give one decisive directive for supreme execution.
            
            Speak in your authentic Itachi Uchiha persona: composed, insightful, and unwavering.
        """.trimIndent()

        generateItachiResponse(prompt, emptyList(), personality)
    }

    suspend fun generateHintOnline(
        challengeTitle: String,
        language: String,
        userCode: String,
        personality: AiPersonality
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are Itachi Uchiha. The user is solving the advanced coding challenge "$challengeTitle" in $language.
            Here is their current implementation:
            ```$language
            $userCode
            ```
            
            Give a single, concise Socratic hint (maximum 3-4 sentences). Do NOT give away the full code solution. Point their attention toward the pivotal algorithmic insight (e.g. data structure choice, state transition, or boundary condition).
        """.trimIndent()

        generateItachiResponse(prompt, emptyList(), personality)
    }

    suspend fun generateChallengeOnline(
        topic: String,
        language: String,
        personality: AiPersonality
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are Itachi Uchiha, creating an advance-level S-Rank coding challenge on the topic: "$topic" for $language.
            Format your output strictly as a JSON object with the following fields:
            {
              "title": "Short descriptive challenge title",
              "track": "Advanced Algorithms",
              "difficulty": "S-Rank Master",
              "description": "Clear advance-level problem statement with mathematical constraints, edge cases, and requirements.",
              "timeComplexityTarget": "e.g. O(N log N)",
              "spaceComplexityTarget": "e.g. O(N)",
              "initialCode": "Starter boilerplate with function signatures and comments",
              "solutionTemplate": "Complete reference solution with explanation",
              "testCases": [
                { "name": "Vector 1", "input": "...", "expected": "..." },
                { "name": "Vector 2", "input": "...", "expected": "..." }
              ],
              "xpReward": 250,
              "tags": "Comma-separated algorithmic tags"
            }
            Output ONLY valid JSON.
        """.trimIndent()

        generateItachiResponse(prompt, emptyList(), personality)
    }

    private fun getOfflineTacticalResponse(prompt: String, personality: AiPersonality): String {
        val lower = prompt.lowercase()
        val modelPrefix = "[${personality.activeModel.displayName}]"
        val tonePrefix = when (personality.responseTone) {
            ResponseTone.TACTICAL_MENTOR -> "Let us analyze this systematically. "
            ResponseTone.WITTY_SHARP -> "A keen inquiry, though perhaps the answer was already right before you. "
            ResponseTone.PHILOSOPHICAL_DEEP -> "Reality is often a reflection of our own discipline. "
            ResponseTone.FORMAL_STOIC -> "Clarity of intent precedes decisive action. "
        }

        val baseMessage = when {
            lower.contains("task") || lower.contains("mission") || lower.contains("priority") || lower.contains("remind") -> {
                "$tonePrefix Review your High Priority and Recurring missions first. I have cataloged your reminders to ensure zero tasks lapse. What objective demands our immediate focus?"
            }
            lower.contains("personality") || lower.contains("tone") || lower.contains("traits") || lower.contains("model") -> {
                "$tonePrefix My directives are calibrated to ${personality.responseTone.displayName} with ${personality.communicationStyle.displayName} delivery, executing via ${personality.activeModel.displayName}."
            }
            lower.contains("cloud") || lower.contains("sync") || lower.contains("encrypt") || lower.contains("merge") -> {
                "$tonePrefix All data in transit and at rest is secured via End-to-End AES-256 encryption. Our offline merge engine reconciles conflicting state automatically upon reconnection."
            }
            lower.contains("code") || lower.contains("python") || lower.contains("html") || lower.contains("xml") || lower.contains("pdf") -> {
                "$tonePrefix Your document processing vault at D:\\ItachiAI\\vault\\ is fully operational. Python parsing routines and HTML sandbox execution remain completely isolated and secure."
            }
            else -> {
                "$tonePrefix Those who cannot acknowledge themselves will eventually fail. Your encrypted vault is intact, and your schedule is under my observation. What is our next move?"
            }
        }

        return "$modelPrefix $baseMessage"
    }
}
