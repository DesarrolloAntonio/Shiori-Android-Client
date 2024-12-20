package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import android.content.Context
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.MainActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarkEditorActivity : ComponentActivity() {

    private val bookmarkViewModel: BookmarkViewModel by viewModel()

    companion object {
        const val EXTRA_MODE = "extra_mode"

        fun createManualIntent(context: Context): Intent {
            return Intent(context, BookmarkEditorActivity::class.java).apply {
                putExtra(EXTRA_MODE, BookmarkEditorType.ADD_MANUALLY.name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mode = intent.getStringExtra(EXTRA_MODE)
        if (mode == BookmarkEditorType.ADD_MANUALLY.name) {
            setupBookmarkEditor(BookmarkEditorType.ADD_MANUALLY, "", "")
            return
        }

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
                        bookmarkViewModel.autoAddBookmark(sharedUrl, title)
                        Toast.makeText(this@BookmarkEditorActivity, "Bookmark saved", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        setupBookmarkEditor(BookmarkEditorType.ADD, sharedUrl, title)
                    }
                } else {
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
                Toast.makeText(this@BookmarkEditorActivity, "No shared URL found", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun setupBookmarkEditor(type: BookmarkEditorType, url: String, title: String) {
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
                        pageTitle = if (type == BookmarkEditorType.ADD_MANUALLY) "Add Manually" else "Add",
                        bookmarkEditorType = type,
                        bookmark = Bookmark(
                            url = url,
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




