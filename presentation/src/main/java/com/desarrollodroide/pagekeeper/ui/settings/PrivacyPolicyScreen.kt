package com.desarrollodroide.pagekeeper.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Privacy Policy of PageKeeper\n\n")
                    }
                    append("Effective as of [Insert Date Here].\n\n")
                    append("This Privacy Policy outlines our policies and procedures on the collection, use, and disclosure of personal information. PageKeeper values your privacy and is committed to protecting it through our compliance with this policy. Our app is designed not to collect or store any personal data from our users, ensuring your privacy and security.\n\n")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Data Collection and Use\n\n")
                    }
                    append("As a commitment to your privacy, PageKeeper does not gather, store, or process any personal data. This includes but is not limited to personal identifiers, contact details, usage data, and location information.\n\n")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Third-Party Services\n\n")
                    }
                    append("PageKeeper does not share any personal data with third parties as no personal data is collected. However, users should be aware that third-party services used by the app may collect their own data.\n\n")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Security\n\n")
                    }
                    append("The security of your personal information is important to us. As we do not collect personal data, there is no risk of your personal information being accessed from our app.\n\n")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Changes to This Privacy Policy\n\n")
                    }
                    append("We may update our Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the 'Effective as of' date at the top of this policy. We may also provide notice to you in other ways in our discretion, such as through contact information you have provided.\n\n")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Contact Us\n\n")
                    }
                    append("For questions or concerns about this Privacy Policy, please contact us via email at desarrollodroide@gmail.com or through other communication channels as provided in our app.\n")
                }

                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}