package com.donut.mixgram.util

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import java.util.Random

fun main() {
    val (privatePem, publicLine) = generateEcdsaKeyPair("noname")
    println(privatePem)
    println()
    println("Public key:")
    println(publicLine)
}

/**
 * 生成 ECDSA (P-256) OpenSSH 密钥对
 * @param comment SSH 公钥的备注（例如用户名）
 * @return Pair(私钥PEM, 公钥行)
 */
fun generateEcdsaKeyPair(comment: String = "generated"): Pair<String, String> {
    // 1️⃣ 生成 ECDSA P-256 密钥对
    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(ECGenParameterSpec("secp256r1"))
    val kp = kpg.generateKeyPair()
    val pub = kp.public as ECPublicKey
    val priv = kp.private as ECPrivateKey

    // 2️⃣ 构建公钥 blob
    val publicBlob = makePublicBlob(pub)

    // 3️⃣ 构建 openssh 私钥块
    val opensshKey = makeOpenSshPrivate(pub, priv, publicBlob, comment)
    val privatePem = wrapPem(
        "OPENSSH PRIVATE KEY",
        Base64.getEncoder().encodeToString(opensshKey)
    )

    // 4️⃣ 公钥行
    val publicLine = makePublicLine(pub, comment)

    return privatePem to publicLine
}

/** 构造公钥 blob */
fun makePublicBlob(pub: ECPublicKey): ByteArray {
    val keyType = "ecdsa-sha2-nistp256"
    val curve = "nistp256"
    val x = toFixed(pub.w.affineX, 32)
    val y = toFixed(pub.w.affineY, 32)
    val point = byteArrayOf(0x04) + x + y

    return buildBytes {
        writeString(keyType)
        writeString(curve)
        writeString(point)
    }
}

/** 公钥行格式：ecdsa-sha2-nistp256 AAAA... comment */
fun makePublicLine(pub: ECPublicKey, comment: String): String {
    val blob = Base64.getEncoder().encodeToString(makePublicBlob(pub))
    return "ecdsa-sha2-nistp256 $blob $comment"
}

/** 构造 OpenSSH 私钥块 */
fun makeOpenSshPrivate(
    pub: ECPublicKey,
    priv: ECPrivateKey,
    pubBlob: ByteArray,
    comment: String
): ByteArray {
    val check = Random().nextInt()
    val keyType = "ecdsa-sha2-nistp256"
    val curve = "nistp256"
    val point = byteArrayOf(0x04) + toFixed(pub.w.affineX, 32) + toFixed(pub.w.affineY, 32)
    val scalar = toFixed(priv.s, 32)

    val privateBlock = buildBytes {
        writeInt(check)
        writeInt(check)
        writeString(keyType)
        writeString(curve)
        writeString(point)
        writeString(scalar)
        writeString(comment)
    }.withPadding(8)

    return buildBytes {
        write("openssh-key-v1\u0000".toByteArray())
        writeString("none")      // ciphername
        writeString("none")      // kdfname
        writeString(ByteArray(0))// kdfoptions
        writeInt(1)              // number of keys
        writeString(pubBlob)     // public key
        writeInt(privateBlock.size)
        write(privateBlock)
    }
}

// ---------- 工具 ----------
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

fun toFixed(bi: BigInteger, len: Int): ByteArray {
    val raw = bi.toByteArray()
    return when {
        raw.size == len -> raw
        raw.size == len + 1 && raw[0] == 0.toByte() -> raw.copyOfRange(1, len + 1)
        raw.size < len -> ByteArray(len - raw.size) + raw
        else -> raw.copyOfRange(raw.size - len, raw.size)
    }
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
