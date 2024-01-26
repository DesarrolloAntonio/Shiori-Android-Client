package com.desarrollodroide.pagekeeper

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.navigation.Navigation
import org.koin.android.ext.android.inject
import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import com.desarrollodroide.pagekeeper.extensions.shareEpubFile


class MainActivity : ComponentActivity() {

    private val themeManager: ThemeManager by inject()
    private val storagePermissionCode = 101

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkStoragePermission()
        setContent {
            ComposeSetup(themeManager = themeManager) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Navigation(
                        onFinish = {
                            finish()
                        },
                        openUrlInBrowser = {
                            openUrlInBrowser(it)
                        },
                        shareEpubFile = {
                            shareEpubFile(it)
                        },
                        openDownloadsFolder = {
                            openDownloadsDirectory()
                        }
                    )
                }
            }
        }
    }

    private fun openDownloadsDirectory() {
        startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
    }
    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), storagePermissionCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso fue concedido
            } else {
                // El permiso fue denegado
            }

    }

}
