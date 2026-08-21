package com.desarrollodroide.pagekeeper.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
fun RememberSessionSection(
    checked: MutableState<Boolean>,
    onCheckedChange: ((Boolean) -> Unit),
    modifier: Modifier = Modifier,
) {
    Row(
        // toggleable (rather than clickable + a separate Checkbox click target) merges the row and
        // the box into one checkbox for accessibility services, so it is announced once with the
        // right role and state.
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked.value,
                onValueChange = onCheckedChange,
                role = Role.Checkbox,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Checkbox(checked = checked.value, onCheckedChange = null)
        Text(
            text = "Remember me",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier,
        )
    }
}
