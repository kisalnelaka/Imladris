package com.imladris.feature.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imladris.core.ui.theme.MidnightBlue
import com.imladris.core.ui.theme.SilverGlow
import com.imladris.core.ui.theme.SoftGold

@Composable
fun ReaderScreen(title: String) {
    var isFocusMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!isFocusMode) {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MidnightBlue,
                        titleContentColor = SoftGold
                    )
                )
            }
        },
        bottomBar = {
            ReaderControls(
                isFocusMode = isFocusMode,
                onFocusToggle = { isFocusMode = !isFocusMode }
            )
        },
        containerColor = MidnightBlue
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Chapter I: A Long-expected Party",
                style = MaterialTheme.typography.headlineMedium,
                color = SoftGold,
                modifier = Modifier.graphicsLayer { alpha = if (isFocusMode) 0.5f else 1.0f }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = """
                    When Mr. Bilbo Baggins of Bag End announced that he would shortly be celebrating his eleventy-first birthday with a party of special magnificence, there was much talk and excitement in Hobbiton.
                    
                    Bilbo was very rich and very peculiar, and had been the wonder of the Shire for sixty years, ever since his remarkable disappearance and unexpected return. The riches he had brought back from his travels had now become a local legend, and it was popularly believed, whatever the old folk might say, that the Hill at Bag End was full of tunnels stuffed with treasure.
                    
                    And if that was not enough for fame, there was also his prolonged vigour to marvel at. Time wore on, but it seemed to have little effect on Mr. Baggins. At ninety he was much the same as at fifty. At ninety-nine they began to call him well-preserved; but unchanged would have been nearer the mark.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 28.sp,
                    fontSize = 18.sp
                ),
                color = if (isFocusMode) SilverGlow.copy(alpha = 0.9f) else SilverGlow
            )
        }
    }
}

@Composable
fun ReaderControls(isFocusMode: Boolean, onFocusToggle: () -> Unit) {
    Surface(
        color = if (isFocusMode) Color.Transparent else MidnightBlue.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if (!isFocusMode) {
                Button(onClick = {}) { Text("Highlight") }
                Button(onClick = {}) { Text("Connect") }
            }
            Button(
                onClick = onFocusToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFocusMode) SoftGold.copy(alpha = 0.2f) else SoftGold
                )
            ) {
                Text(if (isFocusMode) "Exit Focus" else "Focus Mode")
            }
        }
    }
}
