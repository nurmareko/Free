package com.dresta0056.free.ui.add

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.R
import com.dresta0056.free.data.CreateItemResult
import com.dresta0056.free.data.ImageProcessingException
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.data.ItemRepositoryProvider
import com.dresta0056.free.network.toUserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddItemViewModel(
    application: Application,
    private val repository: ItemRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState.asStateFlow()

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
            it.copy(imageUri = uri, imageError = false, error = null)
        }
    }

    fun submit() {
        if (_uiState.value.isSubmitting) return

        val current = _uiState.value
        val titleError = current.title.isBlank()
        val descriptionError = current.description.isBlank()
        val locationError = current.location.isBlank()
        val contactError = current.contactInfo.isBlank()
        val imageError = current.imageUri == null

        if (titleError || descriptionError || locationError || contactError || imageError) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    locationError = locationError,
                    contactError = contactError,
                    imageError = imageError,
                    error = null
                )
            }
            return
        }

        val imageUri = current.imageUri ?: return
        val title = current.title.trim()
        val description = current.description.trim()
        val location = current.location.trim()
        val contactInfo = current.contactInfo.trim()

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.createItem(
                title = title,
                description = description,
                location = location,
                contactInfo = contactInfo,
                imageUri = imageUri
            )) {
                is CreateItemResult.Uploaded,
                is CreateItemResult.Queued -> {
                    _uiState.update { it.copy(done = true) }
                }

                is CreateItemResult.Error -> {
                    Log.e("AddItemViewModel", "Unable to add item", result.throwable)
                    val message = if (result.throwable is ImageProcessingException) {
                        getApplication<Application>().getString(R.string.error_process_image)
                    } else {
                        result.throwable.toUserMessage(getApplication<Application>())
                    }
                    _uiState.update { it.copy(error = message) }
                }
            }
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeDone() {
        _uiState.update { it.copy(done = false) }
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
                return AddItemViewModel(
                    application,
                    ItemRepositoryProvider.get(application)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
