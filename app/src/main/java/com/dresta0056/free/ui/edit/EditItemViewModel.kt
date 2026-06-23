package com.dresta0056.free.ui.edit

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.R
import com.dresta0056.free.network.toUserMessage
import com.dresta0056.free.network.ApiService
import com.dresta0056.free.network.Network
import com.dresta0056.free.network.toTextPart
import com.dresta0056.free.util.compressToImagePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditItemViewModel(
    private val itemId: String,
    private val appContext: Context,
    private val api: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState: StateFlow<EditItemUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val item = api.getItem(itemId)
                _uiState.update {
                    it.copy(
                        title = item.title,
                        description = item.description,
                        location = item.location,
                        contactInfo = item.contactInfo,
                        existingImageUrl = item.imageUrl,
                        loaded = true
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        loaded = true,
                        error = exception.toUserMessage(appContext)
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

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val part = if (newImageUri != null) {
                    try {
                        compressToImagePart(appContext, newImageUri)
                    } catch (exception: Exception) {
                        Log.e("EditItemViewModel", "Unable to process selected image", exception)
                        _uiState.update {
                            it.copy(error = appContext.getString(R.string.error_process_image))
                        }
                        return@launch
                    }
                } else {
                    null
                }

                api.updateItem(
                    id = itemId,
                    title = title.toTextPart(),
                    description = description.toTextPart(),
                    location = location.toTextPart(),
                    contactInfo = contactInfo.toTextPart(),
                    image = part
                )
                _uiState.update { it.copy(done = true) }
            } catch (exception: Exception) {
                Log.e("EditItemViewModel", "Unable to update item", exception)
                _uiState.update { it.copy(error = exception.toUserMessage(appContext)) }
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

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditItemViewModel::class.java)) {
                return EditItemViewModel(
                    itemId = itemId,
                    appContext = applicationContext,
                    api = Network.api
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
