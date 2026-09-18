package com.imladris.feature.reader

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.core.domain.math.CircadianTheme
import com.imladris.core.domain.math.ReadingPacingEngine
import com.imladris.core.ui.components.GlassCard
import com.imladris.core.ui.components.ZoomableBox
import com.imladris.core.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    title: String,
    uriString: String?,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.toFloat()
    val heightDp = configuration.screenHeightDp.toFloat()

    var isFocusMode by remember { mutableStateOf(false) }
    var fontSize by remember { mutableFloatStateOf(18f) }
    var isContinuousScroll by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val contentState by viewModel.contentState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val isBionic by viewModel.isBionicReading.collectAsState()
    val currentWpm by viewModel.readingSpeedWpm.collectAsState()
    val theme by viewModel.circadianTheme.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uriString) {
        viewModel.loadContent(uriString, widthDp, heightDp, fontSize)
    }

    val animatedBg by animateColorAsState(
        targetValue = theme.background,
        animationSpec = tween(500),
        label = "bg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBg)
    ) {
        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = !isFocusMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = theme.text,
                                    maxLines = 1
                                )
                                val wordsRemaining = ((totalPages - currentPage) * 250).coerceAtLeast(0)
                                val minutesLeft = ReadingPacingEngine.estimateMinutesRemaining(wordsRemaining, currentWpm)
                                Text(
                                    text = "${currentWpm.toInt()} WPM • ~$minutesLeft min left",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = theme.accent.copy(alpha = 0.8f)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = theme.text)
                            }
                        },
                        actions = {
                            // Bionic Reading Toggle
                            IconButton(onClick = { viewModel.toggleBionicReading() }) {
                                Icon(
                                    imageVector = if (isBionic) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Bionic Saccades",
                                    tint = if (isBionic) SoftGold else theme.text.copy(alpha = 0.5f)
                                )
                            }
                            // Theme Selector
                            IconButton(onClick = { showThemeMenu = true }) {
                                Icon(Icons.Default.Palette, contentDescription = "Themes", tint = theme.text)
                            }
                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false },
                                modifier = Modifier.background(DeepMist)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Dawn Mist", color = SilverGlow) },
                                    onClick = {
                                        viewModel.setCircadianTheme(ReadingPacingEngine.getCircadianTheme(6))
                                        showThemeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rivendell Daylight", color = SilverGlow) },
                                    onClick = {
                                        viewModel.setCircadianTheme(ReadingPacingEngine.getCircadianTheme(12))
                                        showThemeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Golden Twilight", color = SilverGlow) },
                                    onClick = {
                                        viewModel.setCircadianTheme(ReadingPacingEngine.getCircadianTheme(19))
                                        showThemeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Obsidian Midnight", color = SilverGlow) },
                                    onClick = {
                                        viewModel.setCircadianTheme(ReadingPacingEngine.getCircadianTheme(23))
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = !isFocusMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ReaderBottomControlBar(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        fontSize = fontSize,
                        onFontSizeChange = { fontSize = it },
                        onBookmark = { viewModel.addBookmark() },
                        onFocusToggle = { isFocusMode = !isFocusMode },
                        theme = theme
                    )
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(if (isFocusMode) PaddingValues(0.dp) else padding)
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isFocusMode = !isFocusMode
                    }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = theme.accent
                    )
                } else {
                    when (val state = contentState) {
                        is ReaderContent.Text -> {
                            PaginatedTextReader(
                                pages = state.pages,
                                currentPage = currentPage,
                                fontSize = fontSize,
                                isBionic = isBionic,
                                isFocusMode = isFocusMode,
                                theme = theme,
                                onPageChange = { viewModel.onPageChanged(it) }
                            )
                        }
                        is ReaderContent.Pdf -> {
                            PdfContent(state.pageCount, viewModel)
                        }
                        is ReaderContent.Error -> {
                            ErrorContent(state.message)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaginatedTextReader(
    pages: List<String>,
    currentPage: Int,
    fontSize: Float,
    isBionic: Boolean,
    isFocusMode: Boolean,
    theme: CircadianTheme,
    onPageChange: (Int) -> Unit
) {
    if (pages.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = currentPage.coerceIn(0, pages.size - 1),
        pageCount = { pages.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val pageText = pages.getOrElse(pageIndex) { "" }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isFocusMode) 20.dp else 28.dp, vertical = 20.dp)
            ) {
                if (isBionic) {
                    val tokens = remember(pageText) { ReadingPacingEngine.toBionicTokens(pageText) }
                    val annotatedString = remember(tokens) {
                        buildAnnotatedString {
                            tokens.forEach { token ->
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(token.prefix)
                                }
                                append(token.suffix)
                            }
                        }
                    }
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = (fontSize * 1.7).sp,
                            fontSize = fontSize.sp,
                            letterSpacing = 0.4.sp
                        ),
                        color = theme.text,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = pageText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = (fontSize * 1.7).sp,
                            fontSize = fontSize.sp,
                            letterSpacing = 0.4.sp
                        ),
                        color = theme.text,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Discreet Page Indicator at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / ${pages.size}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = theme.text.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ReaderBottomControlBar(
    currentPage: Int,
    totalPages: Int,
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    onBookmark: () -> Unit,
    onFocusToggle: () -> Unit,
    theme: CircadianTheme
) {
    Surface(
        color = theme.surface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Linear progress indicator
            val progress = if (totalPages > 0) (currentPage.toFloat() / totalPages.toFloat()) else 0f
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = theme.accent,
                trackColor = theme.text.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Font sizing
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (fontSize > 12f) onFontSizeChange(fontSize - 2f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Smaller", tint = theme.text)
                    }
                    Text(
                        text = "${fontSize.toInt()}sp",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.text,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { if (fontSize < 36f) onFontSizeChange(fontSize + 2f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Larger", tint = theme.text)
                    }
                }

                // Bookmark
                IconButton(onClick = onBookmark, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Bookmark", tint = theme.accent)
                }

                // Focus Mode Trigger
                Button(
                    onClick = onFocusToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accent,
                        contentColor = theme.background
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.FilterCenterFocus, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Focus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PdfContent(pageCount: Int, viewModel: ReaderViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(pageCount) { index ->
                var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }

                LaunchedEffect(index) {
                    pageBitmap = viewModel.getPageBitmap(index)
                }

                if (pageBitmap != null) {
                    Image(
                        bitmap = pageBitmap!!.asImageBitmap(),
                        contentDescription = "Page ${index + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .background(Color.LightGray.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CelestialBlue.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
