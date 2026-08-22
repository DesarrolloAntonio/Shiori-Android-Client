package com.desarrollodroide.data.local.preferences

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts the few secrets that have to sit in DataStore.
 *
 * The proto store is a plain file in app storage. That is private on a healthy device, but it is
 * readable on a rooted one and it used to end up in `adb backup` before allowBackup was turned off.
 * The key lives in the AndroidKeyStore, so it never appears in the file and cannot be read back out
 * of the process.
 *
 * Values written before this existed are still plaintext. [decrypt] hands those back unchanged
 * rather than failing, and the next write replaces them with a ciphertext.
 */
class SecretCipher {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }

    private fun key(): SecretKey {
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    fun encrypt(plain: String): String {
        if (plain.isEmpty()) return ""
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val body = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            // The iv is generated per encryption and has to travel with the ciphertext.
            val packed = cipher.iv + body
            PREFIX + Base64.encodeToString(packed, Base64.NO_WRAP)
        }.getOrElse {
            // Refusing to store anything would lock the user out of "remember me" entirely. Storing
            // it in the clear is what the app did before, so that is the fallback, not a new risk.
            Log.e(TAG, "Could not encrypt, storing as written", it)
            plain
        }
    }

    fun decrypt(stored: String): String {
        if (stored.isEmpty()) return ""
        if (!stored.startsWith(PREFIX)) return stored // written before this class existed
        return runCatching {
            val packed = Base64.decode(stored.removePrefix(PREFIX), Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key(),
                GCMParameterSpec(TAG_BITS, packed, 0, IV_BYTES),
            )
            String(cipher.doFinal(packed, IV_BYTES, packed.size - IV_BYTES), Charsets.UTF_8)
        }.getOrElse {
            // A key can disappear: the user adds a lock screen, restores a backup, clears keystore.
            // Losing a saved password means logging in again, which is recoverable.
            Log.e(TAG, "Could not decrypt, treating as absent", it)
            ""
        }
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "shiori_preferences"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val PREFIX = "enc1:"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
        const val TAG = "SecretCipher"
    }
}
