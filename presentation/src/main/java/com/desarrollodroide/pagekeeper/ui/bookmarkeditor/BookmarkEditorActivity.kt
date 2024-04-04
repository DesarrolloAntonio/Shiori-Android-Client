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
import androidx.compose.ui.Modifier
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarkEditorActivity : ComponentActivity() {

    private val bookmarkViewModel: BookmarkViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sharedUrl = ""
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
                if (bookmarkViewModel.userHasSession()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                    ) {
                        BookmarkEditorScreen(
                            title = "Add",
                            bookmarkEditorType = BookmarkEditorType.ADD,
                            bookmark = Bookmark(
                                url = sharedUrl,
                                tags = emptyList(),
                                public = if (bookmarkViewModel.getMakeArchivePublic()) 1 else 0,
                                createArchive = bookmarkViewModel.getCreateArchive(),
                                createEbook = bookmarkViewModel.getCreateEbook()
                            ),
                            onBackClick = { finish() },
                            updateBookmark = { }
                        )
                    }
                } else {
                    NotSessionScreen(
                        onClickLogin = {
                            startMainActivity()
                        }
                    )
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}



