package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TaskEntity::class, DocumentEntity::class, ChatMessageEntity::class, SettingsEntity::class, CodingChallengeEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ItachiDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun documentDao(): DocumentDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun settingsDao(): SettingsDao
    abstract fun codingChallengeDao(): CodingChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: ItachiDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ItachiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItachiDatabase::class.java,
                    "itachi_vault.db"
                ).fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: ItachiDatabase) {
                val taskDao = db.taskDao()
                val docDao = db.documentDao()
                val chatDao = db.chatMessageDao()

                // Initial Shinobi Tasks with Expanded Capabilities
                taskDao.insertTask(
                    TaskEntity(
                        title = "Master Tsukuyomi Deep Focus",
                        description = "Allocate 90 minutes of undisturbed deep work on project architecture.",
                        rank = "S-Rank",
                        category = "Focus",
                        priority = "High",
                        recurrence = "Daily",
                        reminderTime = "Today, 18:00",
                        hasReminder = true,
                        isCompleted = false,
                        dueDate = "Today, 18:00",
                        syncStatus = "synced"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Verify Local AES-256 Vault Encryption",
                        description = "Audit cryptographic keys and ensure mobile and D: drive backups remain offline secured.",
                        rank = "A-Rank",
                        category = "Security",
                        priority = "High",
                        recurrence = "Weekly",
                        reminderTime = "Sundays, 20:00",
                        hasReminder = true,
                        isCompleted = true,
                        dueDate = "Completed",
                        syncStatus = "synced"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Synchronize Mobile & PC Workstation",
                        description = "Establish seamless cross-device bridge for tasks, documents, and tactical telemetry.",
                        rank = "A-Rank",
                        category = "Sync",
                        priority = "Medium",
                        recurrence = "Daily",
                        reminderTime = "Tomorrow, 09:00",
                        hasReminder = true,
                        isCompleted = false,
                        dueDate = "Tomorrow, 10:00",
                        syncStatus = "synced"
                    )
                )
                taskDao.insertTask(
                    TaskEntity(
                        title = "Review Document Processing Pipeline",
                        description = "Test Python script parser, XML validator, and responsive HTML/CSS dashboard.",
                        rank = "B-Rank",
                        category = "Code",
                        priority = "Medium",
                        recurrence = "Monthly",
                        reminderTime = "1st of Month, 11:00",
                        hasReminder = false,
                        isCompleted = false,
                        dueDate = "In 2 days",
                        syncStatus = "synced"
                    )
                )

                // Initial Documents (Python, HTML/CSS/JS, XML, PDF notes)
                docDao.insertDocument(
                    DocumentEntity(
                        title = "Python Document & PDF Parser",
                        fileName = "doc_processor.py",
                        fileType = "py",
                        content = """# Itachi AI Document & PDF Processing Engine
import os
import xml.etree.ElementTree as ET

def process_vault_documents(vault_path="D:/ItachiAI/vault"):
    '''Scans local disk D: for encrypted documents and extracts telemetry.'''
    print(f"[Itachi AI] Scanning vault at: {vault_path}")
    status = {"processed": 14, "encrypted_blocks": 48, "security": "AES-256-GCM"}
    print(f"[Status] All documents verified with zero leakage.")
    return status

if __name__ == "__main__":
    process_vault_documents()
""".trimIndent(),
                        filePath = "D:/ItachiAI/vault/doc_processor.py"
                    )
                )

                docDao.insertDocument(
                    DocumentEntity(
                        title = "Fast Reactive Dashboard",
                        fileName = "dashboard.html",
                        fileType = "html",
                        content = """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
  body { background: #120D11; color: #F5EBE6; font-family: sans-serif; padding: 20px; }
  .card { background: #1F151B; border-left: 4px solid #D32F2F; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
  h2 { color: #FF5252; margin-top: 0; }
  .badge { background: #D32F2F; color: #fff; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
  .stats { display: flex; gap: 10px; margin-top: 10px; }
  .stat-box { flex: 1; background: #2A1C24; padding: 10px; border-radius: 6px; text-align: center; }
</style>
</head>
<body>
  <h2>Itachi Uchiha // System Telemetry</h2>
  <div class="card">
    <div><strong>Sync Status:</strong> <span class="badge">SECURED & SYNCED</span></div>
    <div class="stats">
      <div class="stat-box"><div style="font-size:20px; color:#FF5252">100%</div><div>Offline Ready</div></div>
      <div class="stat-box"><div style="font-size:20px; color:#FFA726">AES-256</div><div>Local Encrypted</div></div>
      <div class="stat-box"><div style="font-size:20px; color:#66BB6A">D:\</div><div>Drive Linked</div></div>
    </div>
  </div>
</body>
</html>
""".trimIndent(),
                        filePath = "D:/ItachiAI/vault/dashboard.html"
                    )
                )

                docDao.insertDocument(
                    DocumentEntity(
                        title = "Shinobi Configuration Schema",
                        fileName = "shinobi_config.xml",
                        fileType = "xml",
                        content = """<?xml version="1.0" encoding="UTF-8"?>
<ShinobiVault version="2.0">
    <Owner>aadwin799@gmail.com</Owner>
    <AssistantName>Itachi Uchiha</AssistantName>
    <SecurityPolicy>
        <EncryptionStandard>AES-256-GCM</EncryptionStandard>
        <OfflineFirst>true</OfflineFirst>
        <LocalStoragePath>D:\ItachiAI\vault\</LocalStoragePath>
    </SecurityPolicy>
    <Missions>
        <PriorityMode>MangekyoStrategic</PriorityMode>
        <AutoPurgeLogs>false</AutoPurgeLogs>
    </Missions>
</ShinobiVault>
""".trimIndent(),
                        filePath = "D:/ItachiAI/vault/shinobi_config.xml"
                    )
                )

                docDao.insertDocument(
                    DocumentEntity(
                        title = "Tactical Shinobi Directive PDF",
                        fileName = "tactical_briefing.pdf",
                        fileType = "pdf",
                        content = """[TACTICAL BRIEFING DOCUMENT - CLASSIFIED]
Subject: Strategic Execution & Mental Fortitude
Author: Itachi Uchiha
Target Vault: D:/ItachiAI/vault/tactical_briefing.pdf

1. PRINCIPLE OF CLARITY
Those who cannot acknowledge their limits will inevitably fail. Prioritize high-impact missions (S-Rank) before engaging with trivial distractions.

2. OFFLINE AUTONOMY
Never rely exclusively on distant cloud servers. True strength lies in autonomous preparation. All strategic intelligence remains encrypted within your local hardware.

3. UNWAVERING RESOLVE
Complete every task with disciplined precision. Let nothing cloud your judgment.
""".trimIndent(),
                        filePath = "D:/ItachiAI/vault/tactical_briefing.pdf"
                    )
                )

                // Initial Chat Message
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sender = "itachi",
                        content = "I am Itachi Uchiha. Your tasks, code, and encrypted files are under my watch. All data remains locally protected on your device and synchronized with your PC vault. How shall we direct our focus today?"
                    )
                )

                // Populate Preloaded S-Rank Coding Curriculum
                val codingDao = db.codingChallengeDao()
                val initialChallenges = com.example.data.coding.PreloadedCodingCurriculum.getInitialChallenges()
                codingDao.insertAllChallenges(initialChallenges)
            }
        }
    }
}
