package com.example.data.model

data class CloudSyncConfig(
    val provider: CloudProvider = CloudProvider.ITACHI_SHINOBI_VAULT,
    val endpointUrl: String = "https://vault.itachi-e2ee.local/api/sync",
    val encryptionAlgorithm: String = "AES-256-GCM + PBKDF2 (Zero-Knowledge)",
    val autoSyncOnReconnect: Boolean = true,
    val wifiOnly: Boolean = false,
    val conflictStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
    val lastSyncTimestamp: Long = System.currentTimeMillis() - 3600000,
    val isSyncing: Boolean = false,
    val pendingChangesCount: Int = 0,
    val syncLog: String = "Initial encrypted handshake verified."
)

enum class CloudProvider(val displayName: String, val badge: String, val description: String) {
    ITACHI_SHINOBI_VAULT(
        displayName = "Itachi Shinobi Cloud",
        badge = "E2EE NATIVE",
        description = "End-to-End encrypted decentralized vault built specifically for your devices."
    ),
    GOOGLE_DRIVE(
        displayName = "Google Drive (E2EE Vault)",
        badge = "GOOGLE CLOUD",
        description = "Encrypted binary container stored in your private Google Drive appdata."
    ),
    PRIVATE_WEBDAV(
        displayName = "Private WebDAV / REST Server",
        badge = "SELF-HOSTED",
        description = "Direct encrypted payload push to your personal home server or NAS."
    ),
    LOCAL_DISK_D(
        displayName = "PC Disk D: Synchronization",
        badge = "LOCAL BRIDGE",
        description = "Direct hardware parity bridge with workstation folder D:\\ItachiAI\\vault\\."
    )
}

enum class ConflictResolutionStrategy(val displayName: String, val detail: String) {
    LATEST_TIMESTAMP(
        displayName = "Latest Timestamp Wins",
        detail = "The most recently updated task or document takes precedence."
    ),
    KEEP_LOCAL(
        displayName = "Prioritize Local Device",
        detail = "Preserve on-device edits and create a backup copy of remote version."
    ),
    MANUAL_MERGE(
        displayName = "Seamless Smart Merge",
        detail = "Merge field-level changes automatically; prompt only on direct title collision."
    )
}
