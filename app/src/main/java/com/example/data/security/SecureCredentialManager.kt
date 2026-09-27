package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureCredentialManager(private val context: Context) {

    private val keyStoreAlias = "StudioMasterSecretKey"
    private val keyStoreType = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val ivSize = 12
    private val tagLengthBits = 128

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("studio_secure_credentials", Context.MODE_PRIVATE)
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(keyStoreType).apply { load(null) }
    }

    init {
        ensureSecretKeyExists()
    }

    private fun ensureSecretKeyExists() {
        if (!keyStore.containsAlias(keyStoreAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                keyStoreType
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                keyStoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(parameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(keyStoreAlias, null) as SecretKey
    }

    fun saveApiKey(providerId: String, apiKey: String) {
        if (apiKey.isBlank()) {
            deleteApiKey(providerId)
            return
        }
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(apiKey.toByteArray(Charsets.UTF_8))

        // Store IV + Ciphertext
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
        prefs.edit().putString("provider_key_$providerId", encoded).apply()
    }

    fun getApiKey(providerId: String): String? {
        val encoded = prefs.getString("provider_key_$providerId", null) ?: return null
        return try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size <= ivSize) return null

            val iv = ByteArray(ivSize)
            val ciphertext = ByteArray(combined.size - ivSize)
            System.arraycopy(combined, 0, iv, 0, ivSize)
            System.arraycopy(combined, ivSize, ciphertext, 0, ciphertext.size)

            val cipher = Cipher.getInstance(transformation)
            val spec = GCMParameterSpec(tagLengthBits, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decrypted = cipher.doFinal(ciphertext)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun hasApiKey(providerId: String): Boolean {
        return prefs.contains("provider_key_$providerId")
    }

    fun deleteApiKey(providerId: String) {
        prefs.edit().remove("provider_key_$providerId").apply()
    }

    fun getMaskedApiKey(providerId: String): String {
        val key = getApiKey(providerId)
        if (key.isNullOrBlank()) return "Not configured"
        return if (key.length <= 8) {
            "••••••••"
        } else {
            key.take(4) + "••••••••" + key.takeLast(4)
        }
    }
}
