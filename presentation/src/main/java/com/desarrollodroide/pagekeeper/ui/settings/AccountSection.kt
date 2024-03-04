package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser

@Composable
fun AccountSection(
    onLogout: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    val privacyPolicyUrl =
        "https://www.dropbox.com/scl/fi/lsukil03brxd28edmpiyi/Pagekeeper-Privacy-Policy.md?rlkey=5qfuu6drer4nbnt18fq0mbsxx&dl=0"
    val termsOfUseUrl =
        "https://www.dropbox.com/scl/fi/w425yshv0a5wp167zcbie/TOS-Pagekeeper.md?rlkey=hxi7kdm4opzyhplcykfpwvs1r&dl=0"
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleSmall
            )

            ClickableOption(
                Item(
                    title = "Terms of Use",
                    icon = Icons.Filled.Gavel,
                    onClick =  onNavigateToTermsOfUse)
            )
            ClickableOption(
                Item(title ="Privacy policy",
                    icon = Icons.Filled.Security,
                    onClick =  onNavigateToPrivacyPolicy)
            )
            ClickableOption(
                Item("Logout",
                    icon =  Icons.Filled.Logout,
                    onClick = onLogout),
                color = Color.Red
            )
        }
    }
}
