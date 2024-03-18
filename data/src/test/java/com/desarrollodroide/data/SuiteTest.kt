package com.desarrollodroide.data

import com.desarrollodroide.data.extensions.BookmarkExtensionTest
import com.desarrollodroide.data.local.preferences.SettingsPreferencesDataSourceImplTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@OptIn(ExperimentalCoroutinesApi::class)
@Suite
@SelectClasses(
    BookmarkExtensionTest::class, SettingsPreferencesDataSourceImplTest::class,
)
class SuiteTest
{
}