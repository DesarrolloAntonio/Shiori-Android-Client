package com.desarrollodroide.pagekeeper.ui.login

import com.desarrollodroide.pagekeeper.ui.components.DialogTone
import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.FormMaxWidth
import com.desarrollodroide.pagekeeper.ui.components.LockPortraitOnPhone
import com.desarrollodroide.pagekeeper.ui.components.shouldPlaceBrandingBeside
import com.desarrollodroide.model.User
import androidx.compose.runtime.getValue
import com.desarrollodroide.data.helpers.SHIORI_GITHUB_URL
import com.desarrollodroide.model.LivenessResponse
import com.desarrollodroide.pagekeeper.ui.settings.LinkableText

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onSuccess: (User) -> Unit,
) {
    LockPortraitOnPhone()

    val loginUiState: UiState<User> by loginViewModel.userUiState.collectAsStateWithLifecycle()
    val livenessUiState: UiState<LivenessResponse> by loginViewModel.livenessUiState.collectAsStateWithLifecycle()
    val serverAvailabilityUiState: UiState<LivenessResponse> by loginViewModel.serverAvailabilityUiState.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LoginContent(
            loginUiState = loginUiState,
            checked = loginViewModel.rememberSession,
            userErrorState = loginViewModel.userNameError,
            passwordErrorState = loginViewModel.passwordError,
            urlErrorState = loginViewModel.urlError,
            onClickLoginButton = {
                loginViewModel.checkSystemLiveness()
            },
            onCheckedRememberSessionChange = {
                loginViewModel.rememberSession.value = it
            },
            onSuccess = {
                loginViewModel.clearState()
                onSuccess.invoke(it)
            },
            user = loginViewModel.userName,
            password = loginViewModel.password,
            serverUrl = loginViewModel.serverUrl,
            onClearError = {
                loginViewModel.clearState()
            },
            livenessUiState = livenessUiState,
            serverAvailabilityUiState = serverAvailabilityUiState,
            onClickTestButton = {
                loginViewModel.checkServerAvailability()
            },
            resetServerAvailabilityState = {
                loginViewModel.resetServerAvailabilityUiState()
            }
        )
    }
}

@Composable
fun LoginContent(
    user: MutableState<String>,
    password: MutableState<String>,
    serverUrl: MutableState<String>,
    checked: MutableState<Boolean>,
    urlErrorState: MutableState<Boolean>,
    userErrorState: MutableState<Boolean>,
    passwordErrorState: MutableState<Boolean>,
    onSuccess: (User) -> Unit,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit,
    onClearError: () -> Unit,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    loginUiState: UiState<User>,
    livenessUiState: UiState<LivenessResponse>,
    serverAvailabilityUiState: UiState<LivenessResponse>,
    resetServerAvailabilityState: () -> Unit
) {
    if (loginUiState.isLoading || livenessUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!livenessUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            tone = DialogTone.Danger,
            title = "Error",
            content = livenessUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                onClearError.invoke()
            }
        )
        Log.v("loginUiState", "Error")
    }
    if (!loginUiState.error.isNullOrEmpty()) {
        ConfirmDialog(
            icon = Icons.Default.Error,
            tone = DialogTone.Danger,
            title = "Error",
            content = loginUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                onClearError.invoke()
            }
        )
        Log.v("loginUiState", "Error")
    } else if (loginUiState.data == null && !loginUiState.idle) {
        ContentViews(
            serverUrl = serverUrl,
            urlErrorState = urlErrorState,
            user = user,
            userErrorState = userErrorState,
            password = password,
            passwordErrorState = passwordErrorState,
            onClickLoginButton = onClickLoginButton,
            checked = checked,
            onCheckedRememberSessionChange = onCheckedRememberSessionChange,
            isTestingServer = serverAvailabilityUiState.isLoading,
            onClickTestButton = onClickTestButton,
            serverAvailabilityUiState = serverAvailabilityUiState,
            serverVersion = serverAvailabilityUiState.data?.message?.version ?: "",
            resetServerAvailabilityState = resetServerAvailabilityState
        )
    } else if (loginUiState.data != null) {
        LaunchedEffect(Unit) {
            onSuccess.invoke(loginUiState.data)
        }
    }
}

@Composable
private fun ContentViews(
    serverUrl: MutableState<String>,
    urlErrorState: MutableState<Boolean>,
    user: MutableState<String>,
    userErrorState: MutableState<Boolean>,
    password: MutableState<String>,
    passwordErrorState: MutableState<Boolean>,
    isTestingServer: Boolean,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit,
    checked: MutableState<Boolean>,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    serverAvailabilityUiState: UiState<LivenessResponse>,
    serverVersion: String,
    resetServerAvailabilityState: () -> Unit
) {
    val form: @Composable (Modifier) -> Unit = { formModifier ->
        LoginForm(
            modifier = formModifier,
            serverUrl = serverUrl,
            urlErrorState = urlErrorState,
            user = user,
            userErrorState = userErrorState,
            password = password,
            passwordErrorState = passwordErrorState,
            isTestingServer = isTestingServer,
            onClickLoginButton = onClickLoginButton,
            onClickTestButton = onClickTestButton,
            checked = checked,
            onCheckedRememberSessionChange = onCheckedRememberSessionChange,
            serverAvailabilityUiState = serverAvailabilityUiState,
            serverVersion = serverVersion,
            resetServerAvailabilityState = resetServerAvailabilityState,
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // The layout is decided on the window less its bars, never less the keyboard. With the
        // keyboard counted, a foldable's inner screen flipped from stacked to side by side as soon
        // as a field took focus, and the form jumped out from under the finger.
        val density = LocalDensity.current
        val bars = WindowInsets.systemBars.union(WindowInsets.displayCutout)
        val barsHeight = with(density) { (bars.getTop(this) + bars.getBottom(this)).toDp() }
        val barsWidth = with(density) {
            (bars.getLeft(this, LayoutDirection.Ltr) + bars.getRight(this, LayoutDirection.Ltr)).toDp()
        }
        val availableHeight = maxHeight - barsHeight
        val brandingBeside = shouldPlaceBrandingBeside(maxWidth - barsWidth, availableHeight)

        if (brandingBeside) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(bars)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                LoginHero(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(48.dp),
                    underStatusBar = false,
                    contentHeight = null,
                )
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .imePadding(),
                ) {
                    val viewport = maxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // heightIn against the viewport is what lets this be centred when it fits
                        // and scroll when it does not. A scrollable child is measured with an
                        // unbounded height, so without a floor there is no spare space for an
                        // arrangement to centre anything in and the content pins to the top.
                        Column(
                            modifier = Modifier
                                .widthIn(max = FormMaxWidth)
                                .fillMaxWidth()
                                .heightIn(min = viewport),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            form(Modifier.fillMaxWidth())
                            LoginFooter(modifier = Modifier.padding(top = 20.dp))
                        }
                    }
                }
            }
        } else {
            // The hero gives up height before the button leaves the screen: whatever the form needs
            // under it comes first, and the hero takes what is left, between its floor and the
            // design's 360dp.
            val heroHeight = (availableHeight - FormHeightUnderHero).coerceIn(MinHeroHeight, MaxHeroHeight)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.ime
                            .union(WindowInsets.navigationBars)
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                    ),
            ) {
                val viewport = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // At least the viewport tall, so the footer sits at the bottom of a tall
                    // screen instead of straight under the button.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = viewport),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LoginHero(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp),
                            underStatusBar = true,
                            contentHeight = heroHeight,
                        )
                        form(
                            Modifier
                                .widthIn(max = FormMaxWidth)
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        LoginFooter(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp))
                    }
                }
            }
        }
    }
}

/**
 * The teal block with the name set large: the first thing the app shows, so it carries the
 * redesign's shapes. A cookie turns slowly behind the bookmark mark; a coral flower and an indigo
 * sun sit opposite it.
 *
 * [contentHeight] is the height below the status bar, or null to fill what the parent gives. The
 * shapes that would run into the name are left out as the block gets shorter: first the sun, then
 * the flower, and the name drops a size.
 *
 * [underStatusBar] runs it up under the status bar, edge to edge, and pushes everything down by
 * the bar's height so nothing hides behind the clock.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginHero(
    modifier: Modifier,
    shape: Shape,
    underStatusBar: Boolean,
    contentHeight: Dp?,
) {
    val spin = rememberInfiniteTransition(label = "login hero")
    val rotation by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 30_000, easing = LinearEasing)),
        label = "cookie rotation",
    )
    val scheme = MaterialTheme.colorScheme
    val cookieShape = MaterialShapes.Cookie9Sided.toShape()
    val flowerShape = MaterialShapes.Flower.toShape()
    val sunnyShape = MaterialShapes.Sunny.toShape()

    Box(
        modifier = modifier
            .clip(shape)
            .background(scheme.primaryContainer)
            .then(
                if (underStatusBar) {
                    Modifier.windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                } else {
                    Modifier
                }
            ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (contentHeight != null) Modifier.height(contentHeight) else Modifier.fillMaxHeight()),
        ) {
            // The thresholds are where each shape's bottom meets the top of the name block.
            val large = maxHeight >= 320.dp
            val showFlower = if (large) maxHeight >= 294.dp else maxHeight >= 256.dp
            val showSun = maxHeight >= 346.dp

            // In the short block the name and tagline sit level with the cookie, so it gets smaller
            // and further off the edge: on a 360dp phone it clears "bookmarks," by about 10dp.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = if (large) 20.dp else 12.dp)
                    .offset(x = if (large) 36.dp else 40.dp)
                    .size(if (large) 190.dp else 140.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { rotationZ = rotation }
                        .clip(cookieShape)
                        .background(scheme.primary),
                )
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(if (large) 64.dp else 44.dp),
                )
            }
            if (showFlower) {
                Box(
                    modifier = Modifier
                        .padding(start = 28.dp, top = if (large) 52.dp else 36.dp)
                        .size(if (large) 84.dp else 72.dp)
                        .clip(flowerShape)
                        .background(scheme.tertiaryContainer),
                )
            }
            if (showSun) {
                Box(
                    modifier = Modifier
                        .padding(start = 124.dp, top = 136.dp)
                        .size(52.dp)
                        .clip(sunnyShape)
                        .background(scheme.secondary),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            ) {
                Text(
                    text = "Shiori",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = if (large) 72.sp else 60.sp,
                        lineHeight = if (large) 70.sp else 60.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-3).sp,
                    ),
                    color = scheme.onPrimaryContainer,
                )
                Text(
                    text = "Your self-hosted bookmarks,\nready when you're offline.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun LoginFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Your bookmarks stay on your own server.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        LinkableText(
            text = "Server Setup Guide",
            url = SHIORI_GITHUB_URL
        )
    }
}

@Composable
private fun LoginForm(
    modifier: Modifier,
    serverUrl: MutableState<String>,
    urlErrorState: MutableState<Boolean>,
    user: MutableState<String>,
    userErrorState: MutableState<Boolean>,
    password: MutableState<String>,
    passwordErrorState: MutableState<Boolean>,
    isTestingServer: Boolean,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit,
    checked: MutableState<Boolean>,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    serverAvailabilityUiState: UiState<LivenessResponse>,
    serverVersion: String,
    resetServerAvailabilityState: () -> Unit
) {
    // No card around it any more: the fields are tonal blocks of their own, and a card behind them
    // was a third layer of grey.
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ServerUrlTextField(
            serverUrl = serverUrl,
            serverErrorState = urlErrorState,
            serverAvailabilityUiState = serverAvailabilityUiState,
            serverVersion = serverVersion,
            resetServerAvailabilityState = resetServerAvailabilityState,
            onClick = onClickTestButton,
            isTestingServer = isTestingServer
        )
        UserTextField(
            user = user,
            userErrorState = userErrorState
        )
        PasswordTextField(
            password = password,
            passwordErrorState = passwordErrorState
        )
        RememberSessionSection(
            checked = checked,
            onCheckedChange = onCheckedRememberSessionChange
        )
        LoginButton(
            modifier = Modifier.padding(top = 4.dp),
            user = user,
            userErrorState = userErrorState,
            password = password,
            passwordErrorState = passwordErrorState,
            onClickLoginButton = onClickLoginButton,
            serverErrorState = urlErrorState
        )
    }
}

/**
 * What the form needs under the hero to show the Sign in button: the 24dp gap, three 56dp fields
 * and the checkbox row with their spacing, the 64dp button, and a little room under it.
 */
private val FormHeightUnderHero = 372.dp
private val MinHeroHeight = 200.dp
private val MaxHeroHeight = 360.dp

/** The form filled in, so the previews show it at the size it actually occupies. */
@Composable
private fun LoginContentSample() {
    ShioriTheme(
        dynamicColor = false
    ) {
        LoginContent(
            user = remember { mutableStateOf("User") },
            password = remember { mutableStateOf("Pass") },
            serverUrl = remember { mutableStateOf("ServerUrl") },
            checked = remember { mutableStateOf(true) },
            urlErrorState = remember { mutableStateOf(true) },
            userErrorState = remember { mutableStateOf(true) },
            passwordErrorState = remember { mutableStateOf(true) },
            onSuccess = {},
            onClickLoginButton = {},
            onCheckedRememberSessionChange = {},
            onClearError = {},
            loginUiState = UiState(data = null, idle = false),
            livenessUiState = UiState(false),
            serverAvailabilityUiState = UiState(data = null, idle = false),
            onClickTestButton = {},
            resetServerAvailabilityState = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    LoginContentSample()
}

/** A small phone, 360x640dp: the hero squeezes so Sign in stays on screen. */
@Preview(name = "Small phone", device = "spec:width=360dp,height=640dp,dpi=320", showSystemUi = true)
@Composable
private fun SmallPhonePreview() {
    LoginContentSample()
}

/**
 * A tablet in landscape: 960x600dp, the Pixel Tablet on its side.
 *
 * This is the layout the branding-beside-the-form rule exists for. Stacked, the form wants about
 * 680dp of height and the button ends up under the gesture bar.
 */
@Preview(
    name = "Landscape tablet",
    device = "spec:width=960dp,height=600dp,dpi=320",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Landscape tablet, dark",
    device = "spec:width=960dp,height=600dp,dpi=320",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun LandscapeTabletPreview() {
    LoginContentSample()
}

/**
 * The tightest real case: a 16:9 tablet, 960x540dp.
 *
 * Sixty fewer dp of height than the one above, which was enough to leave only the top edge of the
 * Log in button on screen before the layout split.
 */
@Preview(
    name = "Short landscape tablet",
    device = "spec:width=960dp,height=540dp,dpi=320",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ShortLandscapeTabletPreview() {
    LoginContentSample()
}
