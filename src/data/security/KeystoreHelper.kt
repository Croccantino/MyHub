package com.myhub.app.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts / decrypts tokens using AES-GCM with a key stored in Android Keystore.
 */
object KeystoreHelper {

    private const val KEYSTORE_ALIAS = "myhub_token_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(KEYSTORE_ALIAS, null)
        if (entry is KeyStore.SecretKeyEntry) return entry.secretKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    /** Encrypts [plainText] and returns (cipherTextBase64, ivBase64). */
    fun encrypt(plainText: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv
        return Base64.encodeToString(cipherBytes, Base64.NO_WRAP) to
                Base64.encodeToString(iv, Base64.NO_WRAP)
    }

    /** Decrypts [cipherTextBase64] with the given [ivBase64]. */
    fun decrypt(cipherTextBase64: String, ivBase64: String): String {
        if (cipherTextBase64.isBlank() || ivBase64.isBlank()) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val plainBytes = cipher.doFinal(Base64.decode(cipherTextBase64, Base64.NO_WRAP))
        return String(plainBytes, Charsets.UTF_8)
    }
}
