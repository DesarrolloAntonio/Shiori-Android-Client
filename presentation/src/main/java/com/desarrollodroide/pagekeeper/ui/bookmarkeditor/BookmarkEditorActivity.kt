package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.MainActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarkEditorActivity : ComponentActivity() {

    private val bookmarkViewModel: BookmarkViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sharedUrl = ""
        var title = ""
        intent?.let { intent ->
            if (intent.action == Intent.ACTION_SEND) {
                intent.extras?.keySet()?.forEach { key ->
                    val value = intent.extras?.get(key)
                    Log.v("Intent Extra", "$key: $value")
                }
                sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                Log.v("Shared link", sharedUrl)
                title = intent.getStringExtra(Intent.EXTRA_TITLE) ?: sharedUrl
                Log.v("Shared title", title)
            } else {
                Toast.makeText(this, "Invalid shared link", Toast.LENGTH_LONG).show()
                finish()
            }
        }
        lifecycleScope.launch {
            bookmarkViewModel.toastMessage.collect { message ->
                message?.let {
                    Toast.makeText(this@BookmarkEditorActivity, it, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        lifecycleScope.launch {
            if (sharedUrl.isNotEmpty()) {
                if (bookmarkViewModel.userHasSession()) {
                    if (bookmarkViewModel.autoAddBookmark) {
                        // Auto-add bookmark without showing the editor screen
                        bookmarkViewModel.autoAddBookmark(sharedUrl, title)
                        Toast.makeText(this@BookmarkEditorActivity, "Bookmark saved", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        // Show the bookmark editor screen
                        setContent {
                            ShioriTheme {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                                ) {
                                    val makeArchivePublic = bookmarkViewModel.makeArchivePublic
                                    val createEbook = bookmarkViewModel.createEbook
                                    val createArchive = bookmarkViewModel.createArchive
                                    BookmarkEditorScreen(
                                        title = "Add",
                                        bookmarkEditorType = BookmarkEditorType.ADD,
                                        bookmark = Bookmark(
                                            url = sharedUrl,
                                            title = title,
                                            tags = emptyList(),
                                            public = if (makeArchivePublic) 1 else 0,
                                            createArchive = createArchive,
                                            createEbook = createEbook
                                        ),
                                        onBack = { finish() },
                                        updateBookmark = { finish() },
                                        showToast = { message ->
                                            Toast.makeText(this@BookmarkEditorActivity, message, Toast.LENGTH_LONG).show()
                                        },
                                        startMainActivity = { startMainActivity() }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // User doesn't have a session, show login screen
                    setContent {
                        ShioriTheme {
                            NotSessionScreen(
                                onClickLogin = {
                                    startMainActivity()
                                }
                            )
                        }
                    }
                }
            } else {
                // No shared URL, finish the activity
                Toast.makeText(this@BookmarkEditorActivity, "No shared URL found", Toast.LENGTH_LONG).show()
                finish()
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




