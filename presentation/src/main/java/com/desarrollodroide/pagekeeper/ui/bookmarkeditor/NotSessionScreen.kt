package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

@Composable
fun NotSessionScreen(
    onClickLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_authentication_failed),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier.padding(40.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            text = "Session not found, please log in and try again.",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onClickLogin,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            content = {
                Text("Login")
            },
        )
    }
}

@Preview
@Composable
fun NotSessionScreenPreview() {
    ShioriTheme {
        NotSessionScreen(
            onClickLogin = { }
        )
    }
}