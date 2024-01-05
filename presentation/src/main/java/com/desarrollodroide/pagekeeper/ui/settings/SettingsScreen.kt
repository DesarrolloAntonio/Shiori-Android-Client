package com.desarrollodroide.pagekeeper.ui.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.ui.components.ErrorDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    goToLogin: () -> Unit
) {
    val settingsUiState = settingsViewModel.settingsUiState.collectAsState().value

    SettingsContent(
        settingsUiState = settingsUiState,
        onLogout = { settingsViewModel.logout() },
        goToLogin = goToLogin,
        isDarkTheme = settingsViewModel.isDarkTheme(),
        onThemeChanged = {
            settingsViewModel.setTheme()
        }
    )
}

@Composable
fun SettingsContent(
    settingsUiState: UiState<String>,
    onLogout: () -> Unit,
    onThemeChanged: () -> Unit,
    isDarkTheme: Boolean,
    goToLogin: () -> Unit
) {
    if (settingsUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
        Log.v("SettingsContent!!", "settingsUiState.isLoading")
    }
    if (!settingsUiState.error.isNullOrEmpty()) {
        ErrorDialog(
            title = "Error",
            content = settingsUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                goToLogin()
            }
        )
        Log.v("SettingsContent!!", settingsUiState.error)
    } else if (settingsUiState.data == null ) {
        Log.v("SettingsContent!!", "settingsUiState.data is null")
    } else {
        Log.v("SettingsContent!!", "settingsUiState.data is not null")
        LaunchedEffect(Unit) {
            goToLogin()
        }
    }
    val privacyPolicyUrl = "https://www.dropbox.com/scl/fi/lsukil03brxd28edmpiyi/Pagekeeper-Privacy-Policy.md?rlkey=5qfuu6drer4nbnt18fq0mbsxx&dl=0"
    val termsOfUseUrl = "https://www.dropbox.com/scl/fi/w425yshv0a5wp167zcbie/TOS-Pagekeeper.md?rlkey=hxi7kdm4opzyhplcykfpwvs1r&dl=0"
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Divider(Modifier.fillMaxWidth(), color = Color.Black, thickness = 1.dp)
        }
        item { ThemeOption(Item("Theme", Icons.Filled.Palette, onClick = {
            onThemeChanged()
        }), darkTheme = isDarkTheme) }
        item { ClickableOption(Item("Terms of Use", Icons.Filled.Gavel) { context.openUrlInBrowser(termsOfUseUrl) }) }
        item { ClickableOption(Item("Privacy policy", Icons.Filled.Security) { context.openUrlInBrowser(privacyPolicyUrl) }) }
        item { ClickableOption(Item("Logout", Icons.Filled.Logout, onClick = onLogout), color = Color.Red) }
    }
}

@Composable
fun ThemeOption(
    item: Item,
    darkTheme: Boolean,
) {
    var isDarkTheme by remember { mutableStateOf(darkTheme) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isDarkTheme = !isDarkTheme
                item.onClick()
                       },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(item.icon, contentDescription = "Change theme")
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = item.title, modifier = Modifier.weight(1f).padding(vertical = 10.dp))
        Icon(if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode, contentDescription = "Change theme")
    }
}

@Composable
fun ClickableOption(item: Item, color: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(item.icon, contentDescription = item.title)
            Text(
                modifier = Modifier.padding(10.dp),
                text = item.title
            )
    }
}


data class Item(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsContent( onLogout = {}, goToLogin = {}, onThemeChanged = {}, settingsUiState = UiState<String>(isLoading = false), isDarkTheme = false)
}