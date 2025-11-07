package com.donut.mixgram.util.objects

import com.donut.mixfile.server.core.aes.generateRandomByteArray
import com.donut.mixfile.server.core.objects.MixShareInfo.Companion.ENCODER
import com.donut.mixfile.server.core.utils.parseJsonObject
import com.donut.mixfile.server.core.utils.toJsonString
import com.donut.mixgram.core.Core
import com.donut.mixgram.util.CHAT_USER_AVATAR
import com.donut.mixgram.util.CHAT_USER_NAME
import com.donut.mixgram.util.GIT_USER_EMAIL
import com.donut.mixgram.util.GIT_USER_NAME
import com.donut.mixgram.util.catchError
import com.donut.mixgram.util.withBlockingTimeout
import kotlinx.serialization.Serializable

@Serializable
data class ChatGroup(
    val repoUrl: String,
    val sshKey: String,
    val name: String,
    val aesKey: String = ENCODER.encode(generateRandomByteArray(32)),
    val commitsLimit: Int = 1000,
    val date: Long = System.currentTimeMillis(),
    val messageSent: Int = 0,
) {
    @kotlinx.serialization.Transient
    var timeout = 1000 * 10L

    companion object {
        fun parseShareCode(shareCode: String): ChatGroup? {
            catchError {
                val json =
                    ENCODER.decode(shareCode.substringAfter("://").trim()).decodeToString()
                return json.parseJsonObject()
            }
            return null
        }
    }

    fun getShareCode() = this.toJsonString().let {
        "mixgram://" + ENCODER.encode(it.encodeToByteArray())
    }

    suspend fun sendMessage(
        message: List<String>,
        userName: String = CHAT_USER_NAME,
        avatar: String? = CHAT_USER_AVATAR
    ) {
        val userMsg = UserMessage(
            userName = userName,
            avatar = avatar,
            message = message,
            group = this
        )

        val encryptedMsg = userMsg.encrypt(aesKey)
        pushCommit(encryptedMsg)
    }

    suspend fun editMessage(
        message: UserMessage,
        newMessage: List<String>,
        userName: String = CHAT_USER_NAME,
        avatar: String? = CHAT_USER_AVATAR,
    ): Boolean {

        val hash = message.commitMessage?.hash ?: return false

        val userMsg = UserMessage(
            userName = userName,
            avatar = avatar,
            message = newMessage,
            group = this
        )
        val encryptedMsg = userMsg.encrypt(aesKey)
        editCommit(hash, encryptedMsg)
        return true
    }

    suspend fun deleteMessage(message: UserMessage): Boolean {
        val hash = message.commitMessage?.hash ?: return false
        deleteCommit(hash)
        return true
    }


    suspend fun fetchMessages(): List<UserMessage>? {
        val commits = this.fetchCommits() ?: return null
        val key = ENCODER.decode(aesKey)
        val messageList = commits.map { it.decrypt(key, this) }
        return messageList
    }

    fun updateAuthorInfo() {
        Core.setUserName(GIT_USER_NAME)
        Core.setUserEmail(GIT_USER_EMAIL)
    }


    suspend fun trimCommits(keep: Int) = withBlockingTimeout(timeout) {
        updateAuthorInfo()
        Core.trimOldCommits(repoUrl, sshKey, keep.toLong())
    }


    suspend fun fetchCommits(): List<CommitMessage>? =
        withBlockingTimeout(timeout) {
            Core.fetchCommitsJSON(repoUrl, sshKey, 0).parseJsonObject<List<CommitMessage>>()
        }


    suspend fun pushCommit(message: String) =
        withBlockingTimeout(timeout) {
            updateAuthorInfo()
            Core.pushCommit(repoUrl, sshKey, message)
        }


    suspend fun editCommit(hash: String, message: String) =
        withBlockingTimeout(timeout) {
            updateAuthorInfo()
            Core.modifyCommit(repoUrl, sshKey, hash, message)
        }


    suspend fun deleteCommit(hash: String) =
        withBlockingTimeout(timeout) {
            updateAuthorInfo()
            Core.deleteCommit(repoUrl, sshKey, hash)
        }

}