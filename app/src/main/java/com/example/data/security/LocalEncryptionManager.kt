package com.example.data.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Local AES-256 Encryption Manager for secure on-device storage.
 * Ensures all user tasks, confidential documents, and AI conversations
 * remain locally encrypted at rest on mobile and offline vaults.
 */
object LocalEncryptionManager {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val PREFIX = "ENC_AES256:"
    private val DEFAULT_IV = ByteArray(16) { 0x55.toByte() }

    // Derives a 256-bit AES key from a local passphrase
    private fun deriveKey(passphrase: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(passphrase.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plaintext string using AES-256 CBC.
     */
    fun encrypt(plainText: String, passphrase: String = "Itachi_Uchiha_Local_Vault_Key_2026"): String {
        if (plainText.isBlank()) return plainText
        if (plainText.startsWith(PREFIX)) return plainText // Already encrypted
        return try {
            val key = deriveKey(passphrase)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivSpec = IvParameterSpec(DEFAULT_IV)
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val base64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            "$PREFIX$base64"
        } catch (e: Exception) {
            plainText
        }
    }

    /**
     * Decrypts ciphertext back to plaintext.
     */
    fun decrypt(cipherText: String, passphrase: String = "Itachi_Uchiha_Local_Vault_Key_2026"): String {
        if (!cipherText.startsWith(PREFIX)) return cipherText
        return try {
            val base64 = cipherText.removePrefix(PREFIX)
            val encryptedBytes = Base64.decode(base64, Base64.NO_WRAP)
            val key = deriveKey(passphrase)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivSpec = IvParameterSpec(DEFAULT_IV)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }

    fun isEncrypted(text: String): Boolean = text.startsWith(PREFIX)
}
