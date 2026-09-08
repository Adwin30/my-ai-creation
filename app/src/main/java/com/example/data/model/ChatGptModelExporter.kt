package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object ChatGptModelExporter {

    /**
     * Formats a single message with ChatGPT model context, system persona, and instructions.
     */
    fun formatMessageWithChatGptModel(
        messageText: String,
        isUser: Boolean,
        model: AiModel,
        personality: AiPersonality
    ): String {
        val role = if (isUser) "User" else "Assistant (Itachi Uchiha • ${model.displayName})"
        return buildString {
            appendLine("### ChatGPT Model Prompt [${model.displayName}]")
            appendLine("**Target Model:** ${model.modelTag} (${model.provider})")
            appendLine("**System Persona:** Itachi Uchiha")
            appendLine("- Tone: ${personality.responseTone.displayName}")
            appendLine("- Style: ${personality.communicationStyle.displayName}")
            appendLine("- Proactivity: ${personality.proactivityLevel.displayName}")
            appendLine("- Directives: ${personality.customDirectives}")
            appendLine("---")
            appendLine("**$role:**")
            appendLine(messageText)
        }.trimEnd()
    }

    /**
     * Formats full chat transcript specifically structured for ChatGPT prompts and exports.
     */
    fun formatFullConversationWithChatGptModel(
        conversation: List<Pair<String, String>>, // sender ("user" or "itachi") to decrypted text
        model: AiModel,
        personality: AiPersonality
    ): String {
        return buildString {
            appendLine("==========================================")
            appendLine("ITACHI AI • CHATGPT MODEL TRANSCRIPT")
            appendLine("Target Engine: ${model.displayName} (`${model.modelTag}`)")
            appendLine("Provider: ${model.provider}")
            appendLine("Persona Tone: ${personality.responseTone.displayName}")
            appendLine("Communication Style: ${personality.communicationStyle.displayName}")
            appendLine("Proactivity Level: ${personality.proactivityLevel.displayName}")
            appendLine("Security: AES-256 E2EE Local Encrypted Storage")
            appendLine("==========================================")
            appendLine()
            appendLine("### SYSTEM INSTRUCTIONS:")
            appendLine("You are Itachi Uchiha, tactical shinobi strategist, personal mentor, and secure assistant.")
            appendLine("Tone: ${personality.responseTone.displayName} — ${personality.responseTone.description}")
            appendLine("Style: ${personality.communicationStyle.displayName} — ${personality.communicationStyle.detail}")
            appendLine("Custom Directives: ${personality.customDirectives}")
            appendLine()
            appendLine("### CONVERSATION TURNS:")
            for ((sender, text) in conversation) {
                val label = if (sender == "user") "USER" else "ITACHI UCHIHA (${model.displayName})"
                appendLine("[$label]:")
                appendLine(text)
                appendLine()
            }
            appendLine("--- End of ChatGPT Model Export ---")
        }.trimEnd()
    }

    /**
     * Formats conversation into valid OpenAI Chat Completion API JSON format.
     */
    fun formatAsOpenAiJson(
        conversation: List<Pair<String, String>>,
        model: AiModel,
        personality: AiPersonality
    ): String {
        val root = JSONObject()
        root.put("model", model.modelTag)

        val messages = JSONArray()
        // System turn
        val systemObj = JSONObject().apply {
            put("role", "system")
            put(
                "content",
                "You are Itachi Uchiha, tactical shinobi strategist and personal AI assistant. " +
                "Tone: ${personality.responseTone.displayName}. " +
                "Style: ${personality.communicationStyle.displayName}. " +
                "Proactivity: ${personality.proactivityLevel.displayName}. " +
                "Directives: ${personality.customDirectives}"
            )
        }
        messages.put(systemObj)

        for ((sender, text) in conversation) {
            val msgObj = JSONObject().apply {
                put("role", if (sender == "user") "user" else "assistant")
                put("content", text)
            }
            messages.put(msgObj)
        }

        root.put("messages", messages)
        root.put("temperature", 0.7)
        return root.toString(2)
    }
}
