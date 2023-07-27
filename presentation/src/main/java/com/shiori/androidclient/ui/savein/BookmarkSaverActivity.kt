package com.shiori.androidclient.ui.savein

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shiori.androidclient.ui.theme.ShioriTheme
import com.shiori.model.Tag
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarkSaverActivity : ComponentActivity() {

    private val viewModel: BookmarkViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sharedUrl =  ""
        intent?.let { intent ->
            if (intent.action == Intent.ACTION_SEND) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    sharedUrl = it
                    Log.v("Shared link", it)
                }
            }
        }
        setContent {
            ShioriTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                ){
                    val assignedTags: MutableState<List<Tag>> = remember { mutableStateOf(emptyList()) }
                    BookmarkEditorScreen(
                        sharedUrl = sharedUrl,
                        assignedTags = assignedTags ,
                        saveBookmark = {
                            viewModel.saveBookmark(sharedUrl, assignedTags.value)
                        },
                        onFinishActivity = { finish() }
                    )
                }
            }
        }
    }
}

