package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser

@Composable
fun LinkableText(
    text: String,
    url: String
) {
    val annotatedText = buildAnnotatedString {
        pushStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        append(text)
        pop()
    }
    val context = LocalContext.current
    ClickableText(
        text = annotatedText,
        onClick = {
            context.openUrlInBrowser(url)
        }
    )
}
