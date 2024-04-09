package com.desarrollodroide.pagekeeper.ui.feed

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HtmlTextViewer(
    htmlString: String,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Viewer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            val webViewClient = remember { WebViewClient() }
            // Remember and hold a reference to the WebView instance
            val webViewInstance = remember { mutableStateOf<WebView?>(null) }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        this.webViewClient = webViewClient
                        settings.loadWithOverviewMode = true
                        loadDataWithBaseURL(null, htmlString, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    webViewInstance.value = webView // Update the remembered WebView instance
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { webViewInstance.value?.goBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                IconButton(onClick = { webViewInstance.value?.goForward() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                }
                IconButton(onClick = { webViewInstance.value?.reload() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
        }
    }
}

@Preview
@Composable
fun HtmlTextViewerPreview() {
    val html = """
   <div id="readability-page-1" class="page"><div><h2 id="825f"><a href="http://android-developers.googleblog.com/2024/02/first-developer-preview-android15.html" rel="noopener ugc nofollow" target="_blank">The First Developer Preview of Android 15</a> 🧑‍💻</h2><figure></figure><p id="bbff">We released the first Developer Preview of Android 15, which focuses on providing access to superior media capabilities, minimizing battery impact, maintaining buttery smooth app performance, and protecting user privacy/security — all while enabling a diverse ecosystem of devices.</p><p id="2588">Android 15 includes updates to <a href="https://developer.android.com/design-for-safety/privacy-sandbox" rel="noopener ugc nofollow" target="_blank">Privacy Sandbox</a> and <a href="https://developer.android.com/health-and-fitness/guides/health-connect" rel="noopener ugc nofollow" target="_blank">Health Connect</a>, while introducing new <a href="https://developer.android.com/reference/android/security/FileIntegrityManager" rel="noopener ugc nofollow" target="_blank">file integrity protection APIs</a>. It provides <a href="https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics" rel="noopener ugc nofollow" target="_blank">enhanced camera controls</a> and <a href="https://developer.android.com/reference/android/media/midi/MidiUmpDeviceService" rel="noopener ugc nofollow" target="_blank">virtual MIDI 2.0 devices</a> to help power creative applications. It expands the <a href="https://developer.android.com/games/optimize/adpf" rel="noopener ugc nofollow" target="_blank">Android Dynamic Performance Framework</a> to support a <a href="https://developer.android.com/reference/android/os/PerformanceHintManager.Session#setPreferPowerEfficiency(boolean)" rel="noopener ugc nofollow" target="_blank">power-efficiency mode</a>, <a href="https://developer.android.com/reference/android/os/WorkDuration#setActualGpuDurationNanos(long)" rel="noopener ugc nofollow" target="_blank">report GPU work durations</a>, and return <a href="https://developer.android.com/reference/android/os/PowerManager#getThermalHeadroomThresholds()" rel="noopener ugc nofollow" target="_blank">Thermal Headroom thresholds</a>. It adds quality of life focused OpenJDK APIs that will be updated on over a billion devices through Google Play system updates.</p><p id="a742"><a href="https://developer.android.com/about/versions/15/get" rel="noopener ugc nofollow" target="_blank">Get started today</a> testing your app with Android 15 in the emulator, or by flashing a system image onto a Pixel 6+, Pixel Fold, or Pixel Tablet device.</p><h2 id="bf5a"><a href="http://android-developers.googleblog.com/2024/02/android-studio-iguana-is-stable.html" rel="noopener ugc nofollow" target="_blank">Android Studio Iguana launched to stable</a>🦎</h2><figure></figure><p id="8baf">We launched <a href="https://developer.android.com/studio/releases" rel="noopener ugc nofollow" target="_blank">Android Studio Iguana 🦎</a> in the stable release channel to make it easier for you to create high quality apps.</p><p id="715c">Enhancements include <a href="https://developer.android.com/studio/releases#compose-ui-check" rel="noopener ugc nofollow" target="_blank">Compose UI Check</a>, which automatically audits Compose UI for accessibility and adaptive issues across different screen sizes. <a href="https://developer.android.com/studio/releases#compose-progressive-rendering" rel="noopener ugc nofollow" target="_blank">Progressive rendering in Compose Preview</a> which speeds up iteration on complex layouts by lowering the detail of out-of-view previews. Iguana adds <a href="https://developer.android.com/studio/releases#aqi-vcs" rel="noopener ugc nofollow" target="_blank">Version Control System support in App Quality Insights</a>, <a href="https://developer.android.com/studio/releases#baseline-profiles-module-wizard" rel="noopener ugc nofollow" target="_blank">built-in support to create Baseline Profiles</a>, and enhanced support for Gradle Version Catalogs. The <a href="https://developer.android.com/studio/releases#espresso-device-api" rel="noopener ugc nofollow" target="_blank">Espresso device API</a> enables configuration change testing. The integrated <a href="https://developer.android.com/studio/releases#intellij-platform-update" rel="noopener ugc nofollow" target="_blank">IntelliJ 2023.2 update</a> includes many enhancements such as support for GitLab and text search in Search Everywhere. The <a href="http://android-developers.googleblog.com/2024/02/android-studio-iguana-is-stable.html" rel="noopener ugc nofollow" target="_blank">blog has information on all these changes</a> and more.</p><h2 id="6888"><a href="http://android-developers.googleblog.com/2024/02/cloud-photos-now-available-in-android-photo-picker.html" rel="noopener ugc nofollow" target="_blank">Cloud photos now available in the Android photo picker</a>☁️📷</h2><figure></figure><p id="a9c8">Android’s <a href="https://developer.android.com/training/data-storage/shared/photopicker" rel="noopener ugc nofollow" target="_blank">photo picker</a> now integrates cloud photos, giving apps a unified way to let users browse and grant access to selected local and cloud photos and videos. It’s currently available integrated with Google Photos and is open to other cloud media apps that meet the eligibility criteria. The cloud photos feature is currently rolling out with the February Google Play system update to devices running Android 12 and above.</p><h2 id="6e01"><a href="http://android-developers.googleblog.com/2024/02/ml-kit-document-scanner-api.html" rel="noopener ugc nofollow" target="_blank">Easily add document scanning capability to your app with the ML Kit Document Scanner API</a>📃📷</h2><figure></figure><p id="707f">We launched the <a href="https://developers.google.com/ml-kit/vision/doc-scanner" rel="noopener ugc nofollow" target="_blank">ML Kit Document Scanner API</a>, enabling you to easily integrate advanced document scanning capabilities into your apps.</p><p id="528f">The API offers a standardized and user-friendly interface for document scanning, includes precise corner and edge detection for accurate document capture, and allows users to further crop scanned documents, apply filters, and remove fingers or blemishes. It processes documents on the device, eliminates the need for camera permissions, and is supported on devices with Android API level 21 or above.</p><h2 id="7e22"><a href="http://android-developers.googleblog.com/2024/02/wear-os-hybrid-interface-boosting-power-and-performance.html" rel="noopener ugc nofollow" target="_blank">Android Developers Blog: Wear OS hybrid interface: Boosting power and performance</a>⌚</h2><figure></figure><p id="b2d1">The WearOS powered OnePlus Watch 2 launched with a dual-chipset architecture that works with our hybrid interface to dramatically extend battery life up to 100 hours of Smart Mode regular use.</p><p id="fd70">You can leverage existing Wear OS APIs to get the advantage of these optimizations, such as <a href="https://developer.android.com/training/wearables/notifications?_gl=1*9dlcvi*_up*MQ..*_ga*NjY5MzY0MTMzLjE3MDc3ODEwMzU.*_ga_6HH9YJMN9M*MTcwNzc4MTAzNC4xLjAuMTcwNzc4MTAzNC4wLjAuMA..#add-wearable-features" rel="noopener ugc nofollow" target="_blank">NotificationCompat</a>, and <a href="https://developer.android.com/health-and-fitness/guides/health-services" rel="noopener ugc nofollow" target="_blank">Health Services on Wear OS</a>. With Wear OS 4, we launched the <a href="https://android-developers.googleblog.com/2023/05/introducing-watch-face-format-for-wear-os.html" rel="noopener ugc nofollow" target="_blank">Watch Face Format</a>, and the new format helps future-proof watch faces to take advantage of emerging optimizations in future devices.</p><h2 id="c0d5">Articles📚</h2><p id="b547">There are a bunch of other articles worth checking out.</p><p id="95e1"><a href="https://medium.com/u/68e2e0af15b1?source=post_page-----46422a7fefe8--------------------------------" rel="noopener" target="_blank">Levi</a> covered <a rel="noopener" href="https://medium.com/androiddevelopers/understanding-nested-scrolling-in-jetpack-compose-eb57c1ea0af0">Nested Scrolling in Jetpack Compose</a>, giving a deep dive into how you can implement custom nested behaviors, such as what the Material 3’s TopAppBar <a href="https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/AppBar.kt;l=653?q=TopAppBarScrollBehavior&amp;sq=&amp;ss=androidx%2Fplatform%2Fframeworks%2Fsupport" rel="noopener ugc nofollow" target="_blank">scrollBehavior</a> parameter does.</p><p id="fa0d"><a href="https://medium.com/u/84718b19bc40?source=post_page-----46422a7fefe8--------------------------------" rel="noopener" target="_blank">Ben</a> explained <a rel="noopener" href="https://medium.com/androiddevelopers/jetpack-compose-strong-skipping-mode-explained-cbdb2aa4b900">Jetpack Compose’s Strong Skipping Mode</a>, an experimental feature in the Jetpack Compose Compiler 1.5.4+ that changes the rules for what composables can skip recomposition which should greatly reduce recomposition, improving performance.</p><p id="f40c"><a href="https://medium.com/u/3f9b9c30bec7?source=post_page-----46422a7fefe8--------------------------------" rel="noopener" target="_blank">Rebecca</a> showed how you can use <a rel="noopener" href="https://medium.com/androiddevelopers/fun-with-shapes-in-compose-8814c439e1a0">shapes in Compose to create a cool progress bar that morphs between two shapes</a> using the <a href="https://developer.android.com/jetpack/compose/graphics/draw/shapes" rel="noopener ugc nofollow" target="_blank">graphics-shapes library</a>, which has <a href="https://developer.android.com/jetpack/compose/graphics/draw/shapes" rel="noopener ugc nofollow" target="_blank">new documentation</a> to help you add these effects into your apps.</p><h2 id="b94c">Videos📹</h2><p id="5a1b">Over in videos, #WeArePlay <a href="https://www.youtube.com/watch?v=CfzhLOiczDQ" rel="noopener ugc nofollow" target="_blank">highlighted the developers behind</a> <a href="https://play.google.com/store/apps/details?id=fr.altplusun.we_spot_turtles" rel="noopener ugc nofollow" target="_blank">We Spot Turtles!</a>, whose app helps crowdsource pictures that a machine learning model uses to help collect extensive data on sea turtles in the wild.</p><figure></figure><p id="aa80">There’s also an associated blog post if you’d rather read about them!</p><h2 id="4c1a">AndroidX releases 🚀</h2><p id="a536">There was a bunch of activity over in Android Jetpack, including the first alphas of Annotation 1.8, Benchmark 1.3, Core-RemoteViews 1.1, Glance 1.1, ProfileInstaller 1.4, Lint 1.0, Wear Watchface 1.3, Webkit 1.11, and Compose Material 3 1.3. Highlights include:</p><ul><li id="ca40">Compose Material 3 1.3 includes more support for predictive back, and updates to the Slider and ProgressIndicator to improve accessibility.</li><li id="e4ad">The new Lint library is a set of lint checks for Gradle Plugin authors on projects that apply java-gradle-plugin to help catch mistakes in their code.</li><li id="18ea">Glance 1.1 adds a new unit test library (that doesn’t require UI Automator), higher level components, new modifiers, and a new API for getting a flow of RemoteViews from a composition.</li></ul><p id="9c42">We also released Hilt Version 1.2 with assisted injection support for hiltViewModel() and hiltNavGraphViewModels() as well as Test Uiautomator 2.3, which adds support for multiple displays and custom wait conditions.</p><h2 id="0de4"><a href="https://adbackstage.libsyn.com/episode-204-fanotations" rel="noopener ugc nofollow" target="_blank">Android Developers Backstage🎙</a></h2><figure></figure><p id="0b93">In <a href="https://adbackstage.libsyn.com/episode-204-fanotations" rel="noopener ugc nofollow" target="_blank">Episode 204: Fan’otations</a> <a href="https://medium.com/u/8251a5f98c9d?source=post_page-----46422a7fefe8--------------------------------" rel="noopener" target="_blank">Tor</a>, <a href="https://medium.com/u/c967b7e51f8b?source=post_page-----46422a7fefe8--------------------------------" rel="noopener" target="_blank">Romain</a>, and <a href="https://medium.com/u/cb2c4874d3e9?source=post_page-----46422a7fefe8--------------------------------" rel="noopener" target="_blank">Chet</a> talk about one of Tor’s favorite topics: Lint! Specifically, they talk about Lint checks and the annotations that use them to enable better, more robust, and more self-documenting APIs.</p><p id="e6b1">As <a href="https://medium.com/u/cb2c4874d3e9?source=post_page-----46422a7fefe8--------------------------------" rel="noopener" target="_blank">Chet</a> says, “Lint: It’s not just for pockets anymore.” Thank you Chet for all you’ve done for Android and the community, and for helping us keep our sense of humor.</p><h2 id="6c1a">Now then… 👋</h2><p id="7d70">That’s it for this week with <a href="http://android-developers.googleblog.com/2024/02/first-developer-preview-android15.html" rel="noopener ugc nofollow" target="_blank">Android 15 developer preview 1</a>, the <a href="http://android-developers.googleblog.com/2024/02/android-studio-iguana-is-stable.html" rel="noopener ugc nofollow" target="_blank">stable release of Android Studio Iguana</a>, <a href="http://android-developers.googleblog.com/2024/02/cloud-photos-now-available-in-android-photo-picker.html" rel="noopener ugc nofollow" target="_blank">cloud photos now available in Photo Picker</a>, <a href="http://android-developers.googleblog.com/2024/02/ml-kit-document-scanner-api.html" rel="noopener ugc nofollow" target="_blank">ML Kit Document Scanning</a>, the <a href="http://android-developers.googleblog.com/2024/02/wear-os-hybrid-interface-boosting-power-and-performance.html" rel="noopener ugc nofollow" target="_blank">Wear OS hybrid interface</a>, <a rel="noopener" href="https://medium.com/androiddevelopers/understanding-nested-scrolling-in-jetpack-compose-eb57c1ea0af0">nested scrolling</a>/<a rel="noopener" href="https://medium.com/androiddevelopers/jetpack-compose-strong-skipping-mode-explained-cbdb2aa4b900">strong skipping</a>/<a rel="noopener" href="https://medium.com/androiddevelopers/fun-with-shapes-in-compose-8814c439e1a0">shape morphing</a> in Compose, <a href="https://adbackstage.libsyn.com/episode-204-fanotations" rel="noopener ugc nofollow" target="_blank">annotations with Lint</a>, and more!</p><p id="5dcd">Check back soon for your next update from the Android developer universe! 🌌</p></div></div>
""".trimIndent()
    MaterialTheme {
        HtmlTextViewer(
            htmlString = html,
            onBack = {}
        )
    }
}
