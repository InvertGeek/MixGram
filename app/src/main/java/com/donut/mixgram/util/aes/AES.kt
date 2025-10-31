package com.donut.mixgram.util.aes

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

fun generateRandomByteArray(size: Int): ByteArray {
    val byteArray = ByteArray(size)
    SecureRandom().nextBytes(byteArray)
    return byteArray
}

fun encryptAES(
    data: ByteArray,
    key: ByteArray,
    iv: ByteArray = generateRandomByteArray(12),
): ByteArray {
    val cipher = getCipher(Cipher.ENCRYPT_MODE, key, iv)
    return iv + cipher.doFinal(data)
}

fun decryptAES(data: ByteArray, key: ByteArray): ByteArray? {
    if (data.size <= 12) {
        return null
    }
    val iv = data.copyOf(12)
    val encryptedData = data.copyOfRange(12, data.size)
    val cipher = getCipher(Cipher.DECRYPT_MODE, key, iv)
    return cipher.doFinal(encryptedData)
}

fun getCipher(mode: Int, key: ByteArray, iv: ByteArray): Cipher {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val secretKey = SecretKeySpec(key, "AES")
    val gcmParameterSpec = GCMParameterSpec(128, iv)
    cipher.init(mode, secretKey, gcmParameterSpec)
    return cipher
}
