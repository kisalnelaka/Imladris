package com.imladris.feature.reader

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.R
import com.imladris.core.ui.components.EtherealBackgroundGlow
import com.imladris.core.ui.components.ReadingVignette
import com.imladris.core.ui.components.ZoomableBox
import com.imladris.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    title: String,
    uriString: String?,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    var isFocusMode by remember { mutableStateOf(false) }
    var fontSize by remember { mutableFloatStateOf(20f) }
    
    val contentState by viewModel.contentState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(uriString) {
        viewModel.loadContent(uriString)
    }

    val backgroundColor by animateColorAsState(
        if (isFocusMode) SoftBlack else MidnightBlue, 
        animationSpec = tween(1000),
        label = "bg"
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        if (!isFocusMode) {
            EtherealBackgroundGlow()
        }

        Scaffold(
            topBar = {
                if (!isFocusMode) {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                text = title, 
                                style = MaterialTheme.typography.headlineMedium,
                                color = CelestialBlue,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            ) 
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = SilverGlow)
                            }
                        },
                        actions = {
                            if (contentState is ReaderContent.Text) {
                                IconButton(onClick = { fontSize += 2f }) { Icon(Icons.Default.Add, null, tint = SilverGlow) }
                                IconButton(onClick = { if (fontSize > 14f) fontSize -= 2f }) { Icon(Icons.Default.Remove, null, tint = SilverGlow) }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                ReaderControls(
                    isFocusMode = isFocusMode,
                    onFocusToggle = { isFocusMode = !isFocusMode }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            val contentPadding = if (isFocusMode) PaddingValues(0.dp) else padding
            
            Box(modifier = Modifier.padding(contentPadding).fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CelestialBlue
                    )
                } else {
                    ZoomableBox(rigidHorizontal = true) { scale, offset ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                        ) {
                            when (val state = contentState) {
                                is ReaderContent.Text -> TextContent(state.content, fontSize, isFocusMode)
                                is ReaderContent.Pdf -> PdfContent(state.pageCount, viewModel)
                                is ReaderContent.Error -> ErrorContent(state.message)
                            }
                        }
                    }
                }
            }
        }
        
        if (!isFocusMode) {
            ReadingVignette(isFocusMode)
        }
    }
}

@Composable
fun TextContent(content: String, fontSize: Float, isFocusMode: Boolean) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = if (isFocusMode) 16.dp else 28.dp, vertical = if (isFocusMode) 16.dp else 24.dp)
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                lineHeight = (fontSize * 1.7).sp,
                fontSize = fontSize.sp,
                letterSpacing = 0.5.sp
            ),
            color = animateColorAsState(
                if (isFocusMode) Moonlight else SilverGlow, 
                animationSpec = tween(800),
                label = "textColor"
            ).value,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(120.dp))
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
                        modifier = Modifier
                            .fillMaxWidth(),
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
                        CircularProgressIndicator(color = CelestialBlue.copy(alpha = 0.2f))
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

@Composable
fun ReaderControls(
    isFocusMode: Boolean,
    onFocusToggle: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (isFocusMode) Color.Transparent else DeepMist.copy(alpha = 0.9f), label = "controls"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = containerColor,
            modifier = Modifier.padding(16.dp),
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onFocusToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFocusMode) SilverGlow.copy(alpha = 0.1f) else CelestialBlue,
                        contentColor = if (isFocusMode) SilverGlow else MidnightBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        if (isFocusMode) Icons.Default.AutoAwesome else Icons.Default.FilterCenterFocus,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(if (isFocusMode) stringResource(R.string.reader_exit_focus) else stringResource(R.string.reader_focus_mode))
                }
            }
        }
    }
}
