package com.dresta0056.free.ui.edit

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.core.AppResult
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.di.ServiceLocator
import com.dresta0056.free.util.compressToImagePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditItemViewModel(
    private val itemId: String,
    private val appContext: Context,
    private val repo: ItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val item = repo.observeItem(itemId).filterNotNull().first()
            _uiState.update { current ->
                if (current.loaded) {
                    current
                } else {
                    current.copy(
                        title = item.title,
                        description = item.description,
                        location = item.location,
                        contactInfo = item.contactInfo,
                        existingImageUrl = item.imageUrl,
                        loaded = true
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update {
            it.copy(title = value, titleError = false, error = null)
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update {
            it.copy(description = value, descriptionError = false, error = null)
        }
    }

    fun onLocationChange(value: String) {
        _uiState.update {
            it.copy(location = value, locationError = false, error = null)
        }
    }

    fun onContactChange(value: String) {
        _uiState.update {
            it.copy(contactInfo = value, contactError = false, error = null)
        }
    }

    fun onImagePicked(uri: Uri) {
        _uiState.update {
            it.copy(newImageUri = uri, error = null)
        }
    }

    fun submit() {
        if (_uiState.value.isSubmitting) return

        val current = _uiState.value
        val titleError = current.title.isBlank()
        val descriptionError = current.description.isBlank()
        val locationError = current.location.isBlank()
        val contactError = current.contactInfo.isBlank()

        if (titleError || descriptionError || locationError || contactError) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    locationError = locationError,
                    contactError = contactError,
                    error = null
                )
            }
            return
        }

        val title = current.title.trim()
        val description = current.description.trim()
        val location = current.location.trim()
        val contactInfo = current.contactInfo.trim()
        val newImageUri = current.newImageUri

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val part = if (newImageUri != null) {
                    try {
                        withContext(Dispatchers.IO) {
                            compressToImagePart(appContext, newImageUri)
                        }
                    } catch (exception: Exception) {
                        Log.e("EditItemViewModel", "Unable to process selected image", exception)
                        _uiState.update { it.copy(error = "Couldn't process the image") }
                        return@launch
                    }
                } else {
                    null
                }

                val result = withContext(Dispatchers.IO) {
                    repo.updateItem(
                        id = itemId,
                        title = title,
                        description = description,
                        location = location,
                        contactInfo = contactInfo,
                        image = part
                    )
                }
                when (result) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(done = true) }
                    }

                    is AppResult.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                }
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeDone() {
        _uiState.update { it.copy(done = false) }
    }

    class Factory(
        private val itemId: String,
        appContext: Context
    ) : ViewModelProvider.Factory {
        private val applicationContext = appContext.applicationContext
        private val repo = ServiceLocator.provideRepository(applicationContext)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditItemViewModel::class.java)) {
                return EditItemViewModel(
                    itemId = itemId,
                    appContext = applicationContext,
                    repo = repo
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
