package com.donut.mixgram.util.objects

import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixfile.server.core.objects.MixShareInfo.Companion.ENCODER
import com.donut.mixfile.server.core.utils.parseJsonObject
import com.donut.mixfile.server.core.utils.toJsonString
import com.donut.mixgram.core.Core
import com.donut.mixgram.util.CHAT_USER_NAME
import com.donut.mixgram.util.aes.decryptAES
import com.donut.mixgram.util.aes.encryptAES
import com.donut.mixgram.util.ignoreError
import kotlinx.serialization.Serializable

@Serializable
data class CommitMessage(
    val hash: String,
    val author: String,
    val email: String,
    val message: String,
    val date: Long
) {

    fun tryDecrypt(key: String): UserMessage? {
        if (message.isEmpty()) {
            return null
        }

        val decoded = ENCODER.decode(message)
        if (decoded.isEmpty()) {
            return null
        }
        val decryptedMessage = decryptAES(decoded, ENCODER.decode(key)) ?: return null

        return decryptedMessage.decodeToString().parseJsonObject<UserMessage>().apply {
            commitMessage = this@CommitMessage
        }
    }

    fun decrypt(key: String): UserMessage {

        ignoreError {
            val result = tryDecrypt(key)
            if (result != null) {
                return result
            }
        }

        return UserMessage(
            date = date,
            userName = author,
            message = listOf(message),
            valid = false,
            commitMessage = this
        )
    }
}


@Serializable
data class UserMessage(
    val date: Long = System.currentTimeMillis(),
    val avatar: MixShareInfo? = null,
    val userName: String = "未知",
    val message: List<String> = listOf(""),
    var valid: Boolean = true,
    @kotlinx.serialization.Transient
    var commitMessage: CommitMessage? = null,
) {

    val isMe get() = userName.contentEquals(CHAT_USER_NAME)

    fun encrypt(key: String): String {
        val encryptedData = encryptAES(this.toJsonString().encodeToByteArray(), ENCODER.decode(key))
        return ENCODER.encode(encryptedData)
    }

}

fun fetchCommits(repoUrl: String, sshKey: String) = Core.fetchCommitsJSON(repoUrl, sshKey, 0)
    .parseJsonObject<List<CommitMessage>>()