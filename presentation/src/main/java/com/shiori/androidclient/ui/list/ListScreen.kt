package com.shiori.androidclient.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.shiori.androidclient.ui.theme.ShioriTheme

@Composable
fun ListScreen() {
    Box (
        modifier = Modifier.fillMaxSize()
    ){
        ListContent()
    }
}

@Composable
fun ListContent(){
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {

            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.inverseOnSurface
            )
        ) {
            Text(
                text = "Login",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListPreview() {
    ShioriTheme() {
        ListContent()
    }
}