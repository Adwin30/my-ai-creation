package com.example.ui

import android.app.Application
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.coding.ExecutionResult
import com.example.data.coding.OfflineCodeExecutor
import com.example.data.coding.PreloadedCodingCurriculum
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CodingChallengeDao
import com.example.data.local.CodingChallengeEntity
import com.example.data.local.DocumentEntity
import com.example.data.local.ItachiDatabase
import com.example.data.local.SettingsEntity
import com.example.data.local.TaskEntity
import com.example.data.model.AiModel
import com.example.data.model.AiPersonality
import com.example.data.model.ChatGptModelExporter
import com.example.data.model.CloudProvider
import com.example.data.model.CloudSyncConfig
import com.example.data.model.CommunicationStyle
import com.example.data.model.ConflictResolutionStrategy
import com.example.data.model.ProactivityLevel
import com.example.data.model.ResponseTone
import com.example.data.remote.GeminiService
import com.example.data.security.LocalEncryptionManager
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ItachiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ItachiDatabase.getDatabase(application, viewModelScope)
    private val taskDao = database.taskDao()
    private val documentDao = database.documentDao()
    private val chatMessageDao = database.chatMessageDao()
    private val settingsDao = database.settingsDao()
    private val geminiService = GeminiService()
    private val cloudSyncManager = CloudSyncManager(taskDao, documentDao)

    // --- Tasks State ---
    val allTasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _taskFilter = MutableStateFlow("All") // "All", "Active", "High Priority", "Recurring", "S-Rank", "Completed"
    val taskFilter: StateFlow<String> = _taskFilter.asStateFlow()

    // --- Active Reminder Alert ---
    private val _activeReminderAlert = MutableStateFlow<TaskEntity?>(null)
    val activeReminderAlert: StateFlow<TaskEntity?> = _activeReminderAlert.asStateFlow()

    // --- AI Personality Customization State ---
    private val _aiPersonality = MutableStateFlow(AiPersonality())
    val aiPersonality: StateFlow<AiPersonality> = _aiPersonality.asStateFlow()

    // --- Cloud Synchronization State (E2EE) ---
    private val _cloudSyncConfig = MutableStateFlow(CloudSyncConfig())
    val cloudSyncConfig: StateFlow<CloudSyncConfig> = _cloudSyncConfig.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<CloudSyncManager.SyncResult?>(null)
    val lastSyncResult: StateFlow<CloudSyncManager.SyncResult?> = _lastSyncResult.asStateFlow()

    // --- Chat State ---
    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // --- Documents State ---
    val allDocuments: StateFlow<List<DocumentEntity>> = documentDao.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDocument = MutableStateFlow<DocumentEntity?>(null)
    val selectedDocument: StateFlow<DocumentEntity?> = _selectedDocument.asStateFlow()

    private val _documentFilter = MutableStateFlow("All") // "All", "py", "html", "xml", "pdf"
    val documentFilter: StateFlow<String> = _documentFilter.asStateFlow()

    // --- Theme & Custom CSS Settings ---
    private val _currentTheme = MutableStateFlow("uchiha_crimson")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _customCss = MutableStateFlow(
        """/* Itachi Uchiha Custom CSS Overrides */
:root {
  --primary: #FF5252;
  --accent: #FFA726;
  --bg: #0D0B0E;
  --font-family: 'Cinzel', serif;
}
.shinobi-card {
  border-left: 4px solid var(--primary);
  background: rgba(22, 18, 23, 0.95);
  box-shadow: 0 4px 20px rgba(255, 82, 82, 0.15);
}
"""
    )
    val customCss: StateFlow<String> = _customCss.asStateFlow()

    private val _customPrimaryColor = MutableStateFlow<Color?>(null)
    val customPrimaryColor: StateFlow<Color?> = _customPrimaryColor.asStateFlow()

    private val _customBgColor = MutableStateFlow<Color?>(null)
    val customBgColor: StateFlow<Color?> = _customBgColor.asStateFlow()

    // --- Sync & Vault Telemetry ---
    val vaultDiskPath = "D:\\ItachiAI\\vault\\"
    private val _isSyncActive = MutableStateFlow(true)
    val isSyncActive: StateFlow<Boolean> = _isSyncActive.asStateFlow()

    private val _syncTelemetry = MutableStateFlow("End-to-End Encrypted Cloud & D: Vault In Parity")
    val syncTelemetry: StateFlow<String> = _syncTelemetry.asStateFlow()

    init {
        loadPersistedSettings()
    }

    private fun loadPersistedSettings() {
        viewModelScope.launch {
            val toneStr = settingsDao.getSetting("personality_tone")
            val styleStr = settingsDao.getSetting("personality_style")
            val proactivityStr = settingsDao.getSetting("personality_proactivity")
            val directivesStr = settingsDao.getSetting("personality_directives")
            val modelStr = settingsDao.getSetting("active_ai_model")
            val openAiKey = settingsDao.getSetting("openai_api_key") ?: ""

            val tone = toneStr?.let { runCatching { ResponseTone.valueOf(it) }.getOrNull() } ?: ResponseTone.FORMAL_STOIC
            val style = styleStr?.let { runCatching { CommunicationStyle.valueOf(it) }.getOrNull() } ?: CommunicationStyle.CONCISE_SHARP
            val proactivity = proactivityStr?.let { runCatching { ProactivityLevel.valueOf(it) }.getOrNull() } ?: ProactivityLevel.HIGH
            val directives = directivesStr ?: "Focus on mission execution, strategic clarity, and zero data leakage."
            val model = modelStr?.let { runCatching { AiModel.valueOf(it) }.getOrNull() } ?: AiModel.CHATGPT_4O

            _aiPersonality.value = AiPersonality(
                activeModel = model,
                responseTone = tone,
                communicationStyle = style,
                proactivityLevel = proactivity,
                customDirectives = directives,
                openAiApiKey = openAiKey
            )

            val providerStr = settingsDao.getSetting("cloud_provider")
            val conflictStr = settingsDao.getSetting("cloud_conflict_strategy")
            val provider = providerStr?.let { runCatching { CloudProvider.valueOf(it) }.getOrNull() } ?: CloudProvider.ITACHI_SHINOBI_VAULT
            val conflict = conflictStr?.let { runCatching { ConflictResolutionStrategy.valueOf(it) }.getOrNull() } ?: ConflictResolutionStrategy.LATEST_TIMESTAMP

            _cloudSyncConfig.value = _cloudSyncConfig.value.copy(
                provider = provider,
                conflictStrategy = conflict
            )
        }
    }

    // --- Task Actions ---
    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    fun addTask(
        title: String,
        description: String,
        rank: String,
        category: String,
        priority: String,
        recurrence: String,
        reminderTime: String,
        hasReminder: Boolean,
        dueDate: String
    ) {
        viewModelScope.launch {
            val encryptedDesc = LocalEncryptionManager.encrypt(description)
            taskDao.insertTask(
                TaskEntity(
                    title = title,
                    description = encryptedDesc,
                    rank = rank,
                    category = category,
                    priority = priority,
                    recurrence = recurrence,
                    reminderTime = reminderTime,
                    hasReminder = hasReminder,
                    isCompleted = false,
                    dueDate = dueDate,
                    isEncrypted = true,
                    syncStatus = "pending",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )

            if (hasReminder && reminderTime.isNotBlank()) {
                // Pre-queue alert reminder for demonstration
                _activeReminderAlert.value = TaskEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    recurrence = recurrence,
                    reminderTime = reminderTime,
                    hasReminder = true
                )
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun updateTaskReminder(task: TaskEntity, reminderTime: String, hasReminder: Boolean) {
        viewModelScope.launch {
            val updated = task.copy(
                reminderTime = reminderTime,
                hasReminder = hasReminder,
                syncStatus = "pending",
                updatedAt = System.currentTimeMillis()
            )
            taskDao.updateTask(updated)
            if (hasReminder) {
                _activeReminderAlert.value = updated
            }
        }
    }

    fun dismissReminderAlert() {
        _activeReminderAlert.value = null
    }

    fun triggerSimulatedReminder(task: TaskEntity) {
        _activeReminderAlert.value = task
    }

    // --- AI Personality Customization Actions ---
    fun setPersonalityTone(tone: ResponseTone) {
        _aiPersonality.value = _aiPersonality.value.copy(responseTone = tone)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("personality_tone", tone.name))
        }
    }

    fun setPersonalityStyle(style: CommunicationStyle) {
        _aiPersonality.value = _aiPersonality.value.copy(communicationStyle = style)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("personality_style", style.name))
        }
    }

    fun setPersonalityProactivity(proactivity: ProactivityLevel) {
        _aiPersonality.value = _aiPersonality.value.copy(proactivityLevel = proactivity)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("personality_proactivity", proactivity.name))
        }
    }

    fun setCustomDirectives(directives: String) {
        _aiPersonality.value = _aiPersonality.value.copy(customDirectives = directives)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("personality_directives", directives))
        }
    }

    fun setActiveAiModel(model: AiModel) {
        _aiPersonality.value = _aiPersonality.value.copy(activeModel = model)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("active_ai_model", model.name))
        }
    }

    fun setOpenAiApiKey(apiKey: String) {
        _aiPersonality.value = _aiPersonality.value.copy(openAiApiKey = apiKey)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("openai_api_key", apiKey))
        }
    }

    // --- Cloud Synchronization & E2EE Actions ---
    fun setCloudProvider(provider: CloudProvider) {
        _cloudSyncConfig.value = _cloudSyncConfig.value.copy(provider = provider)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("cloud_provider", provider.name))
        }
    }

    fun setConflictStrategy(strategy: ConflictResolutionStrategy) {
        _cloudSyncConfig.value = _cloudSyncConfig.value.copy(conflictStrategy = strategy)
        viewModelScope.launch {
            settingsDao.setSetting(SettingsEntity("cloud_conflict_strategy", strategy.name))
        }
    }

    fun setAutoSyncOnReconnect(enabled: Boolean) {
        _cloudSyncConfig.value = _cloudSyncConfig.value.copy(autoSyncOnReconnect = enabled)
    }

    fun setWifiOnly(enabled: Boolean) {
        _cloudSyncConfig.value = _cloudSyncConfig.value.copy(wifiOnly = enabled)
    }

    fun performCloudSync() {
        viewModelScope.launch {
            _cloudSyncConfig.value = _cloudSyncConfig.value.copy(isSyncing = true)
            val result = cloudSyncManager.performE2EeSync(_cloudSyncConfig.value)
            _lastSyncResult.value = result
            _cloudSyncConfig.value = _cloudSyncConfig.value.copy(
                isSyncing = false,
                lastSyncTimestamp = System.currentTimeMillis(),
                syncLog = result.message
            )
            _syncTelemetry.value = "Synced with ${_cloudSyncConfig.value.provider.displayName} (Hash: ${result.e2eeDigest})"
        }
    }

    // --- Chat Actions ---
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            val encryptedUser = LocalEncryptionManager.encrypt(userText)
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    sender = "user",
                    content = encryptedUser,
                    isEncrypted = true
                )
            )

            _isGenerating.value = true

            // Gather context
            val history = chatMessages.value.takeLast(6).map {
                val clean = LocalEncryptionManager.decrypt(it.content)
                it.sender to clean
            }

            val reply = geminiService.generateItachiResponse(
                userPrompt = userText,
                conversationHistory = history,
                personality = _aiPersonality.value
            )
            val encryptedReply = LocalEncryptionManager.encrypt(reply)

            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    sender = "itachi",
                    content = encryptedReply,
                    isEncrypted = true
                )
            )
            _isGenerating.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatMessageDao.clearAllMessages()
            val greeting = "All memory banks purged. I am calibrated to ${_aiPersonality.value.responseTone.displayName} mode. How may I direct your strategy?"
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    sender = "itachi",
                    content = LocalEncryptionManager.encrypt(greeting),
                    isEncrypted = true
                )
            )
        }
    }

    fun askItachiToPrioritizeTasks() {
        val currentTasks = allTasks.value
        val taskSummary = currentTasks.joinToString(separator = "\n") {
            "- [${it.priority} Priority | ${it.rank} | Recurrence: ${it.recurrence}] ${it.title}: ${LocalEncryptionManager.decrypt(it.description)} (Due: ${it.dueDate}, Completed: ${it.isCompleted}, Reminder: ${if (it.hasReminder) it.reminderTime else "None"})"
        }
        val prompt = "Itachi, review my current daily missions including priority levels, recurrences, and reminders. Give me a razor-sharp tactical execution sequence with strategic rationale:\n$taskSummary"
        sendChatMessage(prompt)
    }

    // --- Copy with ChatGPT Model Helpers ---
    fun getMessageWithChatGptModel(message: ChatMessageEntity): String {
        val decrypted = LocalEncryptionManager.decrypt(message.content)
        val isUser = message.sender == "user"
        return ChatGptModelExporter.formatMessageWithChatGptModel(
            messageText = decrypted,
            isUser = isUser,
            model = _aiPersonality.value.activeModel,
            personality = _aiPersonality.value
        )
    }

    fun getFullConversationWithChatGptModel(): String {
        val turns = chatMessages.value.map {
            val decrypted = LocalEncryptionManager.decrypt(it.content)
            it.sender to decrypted
        }
        return ChatGptModelExporter.formatFullConversationWithChatGptModel(
            conversation = turns,
            model = _aiPersonality.value.activeModel,
            personality = _aiPersonality.value
        )
    }

    fun getConversationAsOpenAiJson(): String {
        val turns = chatMessages.value.map {
            val decrypted = LocalEncryptionManager.decrypt(it.content)
            it.sender to decrypted
        }
        return ChatGptModelExporter.formatAsOpenAiJson(
            conversation = turns,
            model = _aiPersonality.value.activeModel,
            personality = _aiPersonality.value
        )
    }

    fun getFullConversationPlainText(): String {
        return chatMessages.value.joinToString("\n\n") {
            val decrypted = LocalEncryptionManager.decrypt(it.content)
            val role = if (it.sender == "user") "User" else "Itachi Uchiha (${_aiPersonality.value.activeModel.displayName})"
            "$role:\n$decrypted"
        }
    }

    // --- Document Actions ---
    fun setDocumentFilter(filter: String) {
        _documentFilter.value = filter
    }

    fun selectDocument(doc: DocumentEntity?) {
        _selectedDocument.value = doc
    }

    fun saveDocument(id: Long, title: String, fileName: String, fileType: String, content: String) {
        viewModelScope.launch {
            val fullPath = "$vaultDiskPath$fileName"
            val entity = DocumentEntity(
                id = id,
                title = title,
                fileName = fileName,
                fileType = fileType,
                content = content,
                filePath = fullPath,
                isEncrypted = true,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                documentDao.insertDocument(entity)
            } else {
                documentDao.updateDocument(entity)
            }
            _selectedDocument.value = entity
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            documentDao.deleteDocument(doc)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = null
            }
        }
    }

    // --- Theme & CSS Actions ---
    fun setTheme(theme: String) {
        _currentTheme.value = theme
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun updateCustomCss(newCss: String) {
        _customCss.value = newCss
        parseCssColors(newCss)
    }

    private fun parseCssColors(css: String) {
        try {
            val primaryRegex = Regex("""--primary:\s*(#[0-9a-fA-F]{6}|#[0-9a-fA-F]{3})""")
            val primaryMatch = primaryRegex.find(css)
            if (primaryMatch != null) {
                val hex = primaryMatch.groupValues[1]
                _customPrimaryColor.value = Color(AndroidColor.parseColor(hex))
            } else {
                _customPrimaryColor.value = null
            }

            val bgRegex = Regex("""--bg:\s*(#[0-9a-fA-F]{6}|#[0-9a-fA-F]{3})""")
            val bgMatch = bgRegex.find(css)
            if (bgMatch != null) {
                val hex = bgMatch.groupValues[1]
                _customBgColor.value = Color(AndroidColor.parseColor(hex))
            } else {
                _customBgColor.value = null
            }
        } catch (e: Exception) {
            // Ignore color parse errors
        }
    }

    fun toggleSync() {
        _isSyncActive.value = !_isSyncActive.value
        if (_isSyncActive.value) {
            performCloudSync()
        } else {
            _syncTelemetry.value = "Offline Air-Gap Mode - Local Storage Only"
        }
    }

    fun generateSyncExportPackage(): String {
        return try {
            val root = JSONObject()
            root.put("vaultPath", vaultDiskPath)
            root.put("provider", _cloudSyncConfig.value.provider.name)
            root.put("encryption", "AES-256-GCM-E2EE")
            root.put("timestamp", System.currentTimeMillis())

            val tasksArray = JSONArray()
            allTasks.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("title", it.title)
                obj.put("desc", it.description)
                obj.put("priority", it.priority)
                obj.put("recurrence", it.recurrence)
                obj.put("reminderTime", it.reminderTime)
                obj.put("hasReminder", it.hasReminder)
                obj.put("rank", it.rank)
                obj.put("completed", it.isCompleted)
                obj.put("syncStatus", "synced")
                tasksArray.put(obj)
            }
            root.put("tasks", tasksArray)

            val docsArray = JSONArray()
            allDocuments.value.forEach {
                val obj = JSONObject()
                obj.put("fileName", it.fileName)
                obj.put("fileType", it.fileType)
                obj.put("content", it.content)
                docsArray.put(obj)
            }
            root.put("documents", docsArray)

            LocalEncryptionManager.encrypt(root.toString(2), "Itachi_E2EE_Cloud_Key_2026")
        } catch (e: Exception) {
            "Sync package error: ${e.message}"
        }
    }
}
