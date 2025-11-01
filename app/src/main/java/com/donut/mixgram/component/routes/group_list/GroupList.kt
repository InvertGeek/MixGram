package com.donut.mixgram.component.routes.group_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.component.common.SingleSelectItemList
import com.donut.mixgram.component.nav.MixNavPage
import com.donut.mixgram.component.routes.group_list.utils.GroupCard
import com.donut.mixgram.component.routes.group_list.utils.createOrAddGroup
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.cachedMutableOf
import com.donut.mixgram.util.compareByName

var groupSort by cachedMutableOf("最新", "mix_group_sort")

val GroupList = MixNavPage(
    gap = 10.dp,
    horizontalAlignment = Alignment.CenterHorizontally,
    floatingButton = {
        FloatingActionButton(onClick = {
            createOrAddGroup()
        }, modifier = Modifier.padding(10.dp, 50.dp)) {
            Icon(Icons.Filled.Add, "Add Group")
        }
    }
) {
    var text by remember {
        mutableStateOf("")
    }
    var groups by remember {
        mutableStateOf(CHAT_GROUPS)
    }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
        },
        modifier = Modifier.fillMaxWidth(), label = {
            Text(text = "输入群组名称")
        },
        maxLines = 3,
        trailingIcon = {
            if (text.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    tint = colorScheme.primary,
                    contentDescription = "clear",

                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        text = ""
                    })
            }
        }
    )

    Text("共 ${CHAT_GROUPS.size} 个群组", color = colorScheme.primary)

    LaunchedEffect(text, groups, groupSort, CHAT_GROUPS) {
        groups = if (text.trim().isNotEmpty()) {
            CHAT_GROUPS.filter {
                it.name.contains(text, true)
            }.asReversed()
        } else {
            CHAT_GROUPS.asReversed()
        }

        when (groupSort) {
            "最新" -> groups = groups.sortedByDescending { it.date }
            "最旧" -> groups = groups.sortedBy { it.date }
            "名称" -> groups = groups.sortedWith { group1, group2 ->
                group1.name.compareByName(group2.name)
            }
        }
    }
    if (groups.isEmpty()) {
        Text(
            text = "没有搜索到群组",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary
        )
        return@MixNavPage
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = "排序: ${groupSort}",
            modifier = Modifier
                .clickable {
                    openSortSelect(groupSort) {
                        groupSort = it
                    }
                }
                .fillMaxWidth()
                .padding(10.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(0.dp, 1000.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(groups.size) { index ->
                val group = groups[index]
                if (index > 0) {
                    HorizontalDivider()
                }
                GroupCard(group)

            }
        }
    }

}

fun openSortSelect(default: String = "", onSelect: (String) -> Unit) {
    MixDialogBuilder("排序选择").apply {
        setContent {
            SingleSelectItemList(listOf("最新", "最旧", "名称"), default) {
                onSelect(it)
                closeDialog()
            }
        }
        show()
    }
}

