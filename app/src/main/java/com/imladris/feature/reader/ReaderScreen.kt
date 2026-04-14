package com.imladris.feature.reader

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.imladris.R
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
    val scrollState = rememberScrollState()
    
    val content by viewModel.content.collectAsState()
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
        EtherealBackgroundGlow()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = title, 
                            style = MaterialTheme.typography.headlineMedium,
                            color = CelestialBlue.copy(alpha = if (isFocusMode) 0.3f else 1f)
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
                        if (!isFocusMode) {
                            IconButton(onClick = { fontSize += 2f }) { Icon(Icons.Default.Add, null, tint = SilverGlow) }
                            IconButton(onClick = { if (fontSize > 14f) fontSize -= 2f }) { Icon(Icons.Default.Remove, null, tint = SilverGlow) }
                        }
                    },
                    modifier = Modifier.graphicsLayer { alpha = if (isFocusMode) 0.2f else 1f }
                )
            },
            bottomBar = {
                ReaderControls(
                    isFocusMode = isFocusMode,
                    onFocusToggle = { isFocusMode = !isFocusMode }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CelestialBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 28.dp, vertical = 24.dp)
                ) {
                    // Content only - no hardcoded headers
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
        }
        
        ReadingVignette(isFocusMode)
    }
}

@Composable
fun EtherealBackgroundGlow() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .background(
                Brush.radialGradient(
                    colors = listOf(CelestialBlue, Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(200f, 200f),
                    radius = 800f
                )
            )
    )
}

@Composable
fun ReadingVignette(isFocusMode: Boolean) {
    val alpha by animateFloatAsState(if (isFocusMode) 0.9f else 0.4f, animationSpec = tween(1000), label = "vignette")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .background(
                Brush.verticalGradient(
                    0f to MidnightBlue,
                    0.15f to Color.Transparent,
                    0.85f to Color.Transparent,
                    1f to MidnightBlue
                )
            )
    )
}

@Composable
fun ReaderControls(
    isFocusMode: Boolean,
    onFocusToggle: () -> Unit
) {
    val containerColor by animateColorAsState(
        if (isFocusMode) Color.Transparent else DeepMist.copy(alpha = 0.9f), label = "controls"
    )
    
    Surface(
        color = containerColor,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onFocusToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFocusMode) SilverGlow.copy(alpha = 0.1f) else CelestialBlue,
                    contentColor = if (isFocusMode) SilverGlow else MidnightBlue
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
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
