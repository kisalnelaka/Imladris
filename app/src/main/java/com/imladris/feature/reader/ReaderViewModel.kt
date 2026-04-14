package com.imladris.feature.reader

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _content = MutableStateFlow<String>("Opening the scrolls...")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadContent(uriString: String?) {
        if (uriString == null) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uri = Uri.parse(uriString)
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.readText()
                        }
                    } ?: "Unable to read the artifact."
                }
                _content.value = text
            } catch (e: Exception) {
                _content.value = "The archives are corrupted or inaccessible: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
