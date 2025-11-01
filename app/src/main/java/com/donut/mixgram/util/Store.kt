package com.donut.mixgram.util

import com.donut.mixfile.server.core.utils.parseJsonObject
import com.donut.mixfile.server.core.utils.toJsonString
import com.donut.mixgram.component.routes.settings.utils.setStringValue
import com.donut.mixgram.util.aes.decryptAES
import com.donut.mixgram.util.aes.encryptAES
import com.donut.mixgram.util.objects.ChatGroup

var CHAT_GROUPS: List<ChatGroup> by cachedMutableOf(listOf(), "MIX_GRAM_GROUPS")


var GIT_USER_NAME by cachedMutableOf("MixGram", "MIX_GRAM_GIT_USER_NAME")

var GIT_USER_EMAIL by cachedMutableOf("admin@mixgram.org", "MIX_GRAM_GIT_USER_EMAIL")

var CHAT_USER_NAME by cachedMutableOf("", "MIX_GRAM_USER_NAME")

var CHAT_USER_AVATAR by cachedMutableOf("", "MIX_GRAM_USER_AVATAR")

var UNLOCK_PASSWORD by cachedMutableOf("", "UNLOCK_PASSWORD")

var ENCRYPTED_DATA by cachedMutableOf("", "ENCRYPTED_GROUPS")

fun encryptGroups() {
    if (UNLOCK_PASSWORD.isEmpty()) {
        return
    }
    val key = UNLOCK_PASSWORD.decodeHex()
    ENCRYPTED_DATA =
        encryptAES(CHAT_GROUPS.toJsonString().encodeToByteArray(), key).encodeToBase64()
    CHAT_GROUPS = listOf()
    UNLOCK_PASSWORD = ""
}

fun decryptGroups(key: String): Boolean {
    val hash = key.hashSHA256()
    if (ENCRYPTED_DATA.isEmpty()) {
        return false
    }
    try {
        val decryptedData = decryptAES(ENCRYPTED_DATA.decodeBase64(), hash) ?: return false
        CHAT_GROUPS = decryptedData.decodeToString().parseJsonObject()
        ENCRYPTED_DATA = ""
        UNLOCK_PASSWORD = hash.toHex()
        return true
    } catch (e: Throwable) {
        return false
    }
}


fun setUnlockPassword() {
    setStringValue("设置密码", "", "解锁密码(留空取消密码)", onFinish = {
        if (it.isBlank()) {
            UNLOCK_PASSWORD = ""
            showToast("成功取消密码")
            return@setStringValue
        }
        UNLOCK_PASSWORD = it.trim().hashSHA256().toHex()
        showToast("设置成功")
    })
}


fun addChatGroup(group: ChatGroup) {
    CHAT_GROUPS = CHAT_GROUPS.filter {
        it.name !== group.name
    }
    CHAT_GROUPS += group
}

fun editChatGroup(group: ChatGroup, newGroup: ChatGroup) {
    CHAT_GROUPS = CHAT_GROUPS.filter {
        it.name !== group.name
    }
    CHAT_GROUPS += newGroup
}