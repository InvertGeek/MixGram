package com.donut.mixgram.util

import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.KeyGenerationParameters
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets
import java.security.Security
import java.util.Base64
import kotlin.random.Random

fun main() {
    val (privatePem, publicLine) = generateKeyPair("noname")
    println(privatePem)
    println()
    println("Public key:")
    println(publicLine)
}

/**
 * 使用 BouncyCastle 生成 ED25519 OpenSSH 密钥对
 */
fun generateKeyPair(comment: String = "generated"): Pair<String, String> {
    // 注册 BouncyCastle 提供者（只需一次）
    Security.addProvider(BouncyCastleProvider())

    // 1️⃣ 生成 ED25519 密钥对
    val generator = Ed25519KeyPairGenerator()
    generator.init(KeyGenerationParameters(java.security.SecureRandom(), 256))
    val pair: AsymmetricCipherKeyPair = generator.generateKeyPair()

    val privateKeyParams = pair.private as Ed25519PrivateKeyParameters
    val publicKeyParams = pair.public as Ed25519PublicKeyParameters

    val privBytes = privateKeyParams.encoded
    val pubBytes = publicKeyParams.encoded

    // 2️⃣ 构建公钥 blob
    val keyType = "ssh-ed25519"
    val pubBlob = buildBytes {
        writeString(keyType)
        writeString(pubBytes)
    }

    // 3️⃣ 构建私钥块（OpenSSH 私钥格式）
    val check = Random.nextInt()
    val privateBlock = buildBytes {
        writeInt(check)
        writeInt(check)
        writeString(keyType)
        writeString(pubBytes)
        writeString(privBytes + pubBytes) // OpenSSH 要求私钥+公钥拼接
        writeString(comment)
    }.withPadding(8)

    // 4️⃣ 构建 openssh-key-v1 格式
    val opensshKey = buildBytes {
        write("openssh-key-v1\u0000".toByteArray())
        writeString("none")      // ciphername
        writeString("none")      // kdfname
        writeString(ByteArray(0))// kdfoptions
        writeInt(1)              // number of keys
        writeString(pubBlob)     // public key
        writeInt(privateBlock.size)
        write(privateBlock)
    }

    // 5️⃣ 输出 PEM 和 公钥行
    val privatePem = wrapPem(
        "OPENSSH PRIVATE KEY",
        Base64.getEncoder().encodeToString(opensshKey)
    )
    val publicLine = "$keyType ${Base64.getEncoder().encodeToString(pubBlob)} $comment"

    return privatePem to publicLine
}

// ---------- 工具函数 ----------

fun buildBytes(block: DataOutputStream.() -> Unit): ByteArray {
    val baos = ByteArrayOutputStream()
    val dout = DataOutputStream(baos)
    dout.block()
    dout.flush()
    return baos.toByteArray()
}

fun DataOutputStream.writeString(s: String) = writeString(s.toByteArray(StandardCharsets.US_ASCII))
fun DataOutputStream.writeString(bytes: ByteArray) {
    writeInt(bytes.size)
    write(bytes)
}

fun DataOutputStream.writeInt(v: Int) {
    write(
        byteArrayOf(
            ((v shr 24) and 0xFF).toByte(),
            ((v shr 16) and 0xFF).toByte(),
            ((v shr 8) and 0xFF).toByte(),
            (v and 0xFF).toByte()
        )
    )
}

fun ByteArray.withPadding(blockSize: Int): ByteArray {
    val padLen = blockSize - (size % blockSize)
    val padding = ByteArray(padLen) { (it + 1).toByte() }
    return this + padding
}

fun wrapPem(type: String, base64: String): String {
    val sb = StringBuilder("-----BEGIN $type-----\n")
    for (i in base64.indices step 70)
        sb.append(base64.substring(i, minOf(i + 70, base64.length))).append('\n')
    sb.append("-----END $type-----")
    return sb.toString()
}
