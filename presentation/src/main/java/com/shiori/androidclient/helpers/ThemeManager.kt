package com.shiori.androidclient.helpers

import androidx.compose.runtime.MutableState

interface ThemeManager {
    var darkTheme: MutableState<Boolean>
}