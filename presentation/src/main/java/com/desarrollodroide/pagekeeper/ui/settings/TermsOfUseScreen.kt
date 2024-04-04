package com.desarrollodroide.pagekeeper.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfUseScreen(
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Terms of Use") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                val termsText = buildAnnotatedString {
                    append("1. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Acceptance of Terms\n\n")
                    }
                    append("By accessing and using PageKeeper, you agree to be bound by these Terms of Use.\n\n")
                    append("2. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("License for the App\n\n")
                    }
                    append("PageKeeper is provided under the Apache 2.0 License, allowing personal and commercial use, redistribution, and modification under the terms specified in the LICENSE file included with the source code.\n\n")
                    append("3. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Disclaimer\n\n")
                    }
                    append("PageKeeper is provided 'as is', without any warranties, expressed or implied, including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose.\n\n")
                    append("4. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Limitations of Liability\n\n")
                    }
                    append("In no event shall PageKeeper, or its contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.\n\n")
                    append("5. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Modifications to Terms\n\n")
                    }
                    append("We may revise these terms of use for PageKeeper at any time without notice. By using this app, you are agreeing to be bound by the then current version of these terms of use.\n\n")
                    append("6. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Governing Law\n\n")
                    }
                    append("Any claim relating to PageKeeper shall be governed by the laws of the app owner's jurisdiction without regard to its conflict of law provisions.\n")
                }


                Text(
                    text = termsText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}