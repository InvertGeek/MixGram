package com.donut.mixgram.util

import com.donut.mixgram.util.objects.ChatGroup

var CHAT_GROUPS: List<ChatGroup> by cachedMutableOf(listOf(), "MIX_GRAM_GROUPS")


var GIT_USER_NAME by cachedMutableOf("MixGram", "MIX_GRAM_GIT_USER_NAME")

var GIT_USER_EMAIL by cachedMutableOf("admin@mixgram.org", "MIX_GRAM_GIT_USER_EMAIL")

var CHAT_USER_NAME by cachedMutableOf("用户${genRandomString(4)}", "MIX_GRAM_USER_NAME")

var CHAT_USER_AVATAR by cachedMutableOf("", "MIX_GRAM_USER_AVATAR")


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