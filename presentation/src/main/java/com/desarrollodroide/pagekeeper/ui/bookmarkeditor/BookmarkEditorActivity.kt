package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

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
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag

class BookmarkEditorActivity : ComponentActivity() {

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
                        title  = "Add",
                        bookmarkEditorType = BookmarkEditorType.ADD,
                        bookmark = Bookmark(sharedUrl, emptyList()),
//                        sharedUrl = sharedUrl,
//                        assignedTags = assignedTags ,
                        onBackClick = { finish() },
                        updateBookmark = {  }
                    )
                }
            }
        }
    }
}

