package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TaskEntity
import com.example.data.model.AiPersonality
import com.example.data.model.CloudProvider
import com.example.data.model.CloudSyncConfig
import com.example.data.model.CommunicationStyle
import com.example.data.model.ConflictResolutionStrategy
import com.example.data.model.ProactivityLevel
import com.example.data.model.ResponseTone
import com.example.data.security.LocalEncryptionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Itachi AI", appName)
  }

  @Test
  fun `test aes encryption and decryption roundtrip`() {
    val secret = "Classified Tsukuyomi Directive"
    val encrypted = LocalEncryptionManager.encrypt(secret)
    val decrypted = LocalEncryptionManager.decrypt(encrypted)
    assertEquals(secret, decrypted)
  }

  @Test
  fun `test task model with priority recurrence and reminders`() {
    val task = TaskEntity(
      id = 1,
      title = "Master Tsukuyomi Deep Focus",
      description = "Encrypted directive",
      rank = "S-Rank",
      category = "Focus",
      priority = "High",
      recurrence = "Daily",
      reminderTime = "Today, 18:00",
      hasReminder = true,
      isCompleted = false,
      dueDate = "Today, 20:00",
      syncStatus = "pending"
    )

    assertEquals("High", task.priority)
    assertEquals("Daily", task.recurrence)
    assertEquals("Today, 18:00", task.reminderTime)
    assertTrue(task.hasReminder)
    assertEquals("pending", task.syncStatus)
  }

  @Test
  fun `test ai personality customization configuration`() {
    val personality = AiPersonality(
      responseTone = ResponseTone.TACTICAL_MENTOR,
      communicationStyle = CommunicationStyle.CONCISE_SHARP,
      proactivityLevel = ProactivityLevel.HIGH,
      customDirectives = "Enforce zero knowledge encryption"
    )

    assertEquals(ResponseTone.TACTICAL_MENTOR, personality.responseTone)
    assertEquals(CommunicationStyle.CONCISE_SHARP, personality.communicationStyle)
    assertEquals(ProactivityLevel.HIGH, personality.proactivityLevel)
    assertEquals("Tactical Mentor", personality.responseTone.displayName)
  }

  @Test
  fun `test cloud sync config with e2ee and conflict strategy`() {
    val config = CloudSyncConfig(
      provider = CloudProvider.ITACHI_SHINOBI_VAULT,
      conflictStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
      autoSyncOnReconnect = true,
      wifiOnly = true
    )

    assertEquals(CloudProvider.ITACHI_SHINOBI_VAULT, config.provider)
    assertEquals(ConflictResolutionStrategy.LATEST_TIMESTAMP, config.conflictStrategy)
    assertTrue(config.autoSyncOnReconnect)
    assertTrue(config.wifiOnly)
  }

  @Test
  fun `test navigation hubs contain only chat and settings`() {
    val hubs = com.example.ui.components.NavigationHub.values()
    assertEquals(2, hubs.size)
    assertEquals(com.example.ui.components.NavigationHub.CHAT, hubs[0])
    assertEquals(com.example.ui.components.NavigationHub.SETTINGS, hubs[1])
  }

  @Test
  fun `test chatgpt model formatting and prompt export`() {
    val personality = AiPersonality(
      activeModel = com.example.data.model.AiModel.CHATGPT_4O,
      responseTone = ResponseTone.FORMAL_STOIC,
      communicationStyle = CommunicationStyle.CONCISE_SHARP,
      proactivityLevel = ProactivityLevel.HIGH,
      customDirectives = "Never surrender clarity"
    )

    val formattedMessage = com.example.data.model.ChatGptModelExporter.formatMessageWithChatGptModel(
      messageText = "Initiate Tsukuyomi tactical breakdown.",
      isUser = true,
      model = personality.activeModel,
      personality = personality
    )

    assertTrue(formattedMessage.contains("ChatGPT Model Prompt [ChatGPT (GPT-4o)]"))
    assertTrue(formattedMessage.contains("gpt-4o"))
    assertTrue(formattedMessage.contains("Never surrender clarity"))
    assertTrue(formattedMessage.contains("Initiate Tsukuyomi tactical breakdown."))
  }

  @Test
  fun `test chatgpt full conversation and openai json payload generation`() {
    val personality = AiPersonality(
      activeModel = com.example.data.model.AiModel.CHATGPT_O1
    )

    val turns = listOf(
      "user" to "What is our mission priority?",
      "itachi" to "Priority is the preservation of encrypted intelligence."
    )

    val fullExport = com.example.data.model.ChatGptModelExporter.formatFullConversationWithChatGptModel(
      conversation = turns,
      model = personality.activeModel,
      personality = personality
    )

    assertTrue(fullExport.contains("ITACHI AI • CHATGPT MODEL TRANSCRIPT"))
    assertTrue(fullExport.contains("ChatGPT (o1 Reasoning)"))
    assertTrue(fullExport.contains("[USER]:"))
    assertTrue(fullExport.contains("[ITACHI UCHIHA (ChatGPT (o1 Reasoning))]:"))

    val jsonString = com.example.data.model.ChatGptModelExporter.formatAsOpenAiJson(
      conversation = turns,
      model = personality.activeModel,
      personality = personality
    )

    val json = org.json.JSONObject(jsonString)
    assertEquals("o1-preview", json.getString("model"))
    val messagesArray = json.getJSONArray("messages")
    assertEquals(3, messagesArray.length()) // system, user, assistant
    assertEquals("system", messagesArray.getJSONObject(0).getString("role"))
    assertEquals("user", messagesArray.getJSONObject(1).getString("role"))
    assertEquals("assistant", messagesArray.getJSONObject(2).getString("role"))
  }
}

