package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RememberSessionSection(
    checked: MutableState<Boolean>,
    onCheckedChange: ((Boolean) -> Unit),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked.value)
            }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked.value,
            onCheckedChange = {
                onCheckedChange.invoke(it)
            }
        )
        Text(
            text = "Remember",
            modifier = Modifier
                .padding(horizontal = 10.dp)
        )
    }
}