package com.dresta0056.free.ui.add

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dresta0056.free.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onDone: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    vm: AddItemViewModel = viewModel(
        factory = AddItemViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val locationFocusRequester = remember { FocusRequester() }
    val contactFocusRequester = remember { FocusRequester() }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            vm.onImagePicked(uri)
        }
    }

    LaunchedEffect(state.error) {
        val message = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        vm.consumeError()
    }

    LaunchedEffect(state.done) {
        if (state.done) {
            onDone()
            vm.consumeDone()
        }
    }

    fun requestFirstIncompleteField() {
        when {
            state.title.isBlank() -> titleFocusRequester.requestFocus()
            state.description.isBlank() -> descriptionFocusRequester.requestFocus()
            state.location.isBlank() -> locationFocusRequester.requestFocus()
            state.contactInfo.isBlank() -> contactFocusRequester.requestFocus()
            else -> focusManager.clearFocus()
        }
    }

    fun submitFromKeyboard() {
        if (
            state.title.isBlank() ||
            state.description.isBlank() ||
            state.location.isBlank() ||
            state.contactInfo.isBlank()
        ) {
            vm.submit()
            requestFirstIncompleteField()
            return
        }
        focusManager.clearFocus()
        vm.submit()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_item_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImagePickerBox(
                state = state,
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            RequiredTextField(
                value = state.title,
                onValueChange = vm::onTitleChange,
                label = stringResource(R.string.field_item_name),
                placeholderText = stringResource(R.string.placeholder_item_name),
                isError = state.titleError,
                errorText = stringResource(R.string.error_item_name_required),
                imeAction = ImeAction.Next,
                onKeyboardAction = { descriptionFocusRequester.requestFocus() },
                modifier = Modifier.focusRequester(titleFocusRequester)
            )
            RequiredTextField(
                value = state.description,
                onValueChange = vm::onDescriptionChange,
                label = stringResource(R.string.field_description),
                placeholderText = stringResource(R.string.placeholder_description),
                isError = state.descriptionError,
                errorText = stringResource(R.string.error_description_required),
                minLines = 4,
                imeAction = ImeAction.Next,
                onKeyboardAction = { locationFocusRequester.requestFocus() },
                modifier = Modifier.focusRequester(descriptionFocusRequester)
            )
            RequiredTextField(
                value = state.location,
                onValueChange = vm::onLocationChange,
                label = stringResource(R.string.field_location),
                placeholderText = stringResource(R.string.placeholder_location),
                isError = state.locationError,
                errorText = stringResource(R.string.error_location_required),
                imeAction = ImeAction.Next,
                onKeyboardAction = { contactFocusRequester.requestFocus() },
                modifier = Modifier.focusRequester(locationFocusRequester)
            )
            RequiredTextField(
                value = state.contactInfo,
                onValueChange = vm::onContactChange,
                label = stringResource(R.string.field_contact_information),
                placeholderText = stringResource(R.string.placeholder_contact_information),
                isError = state.contactError,
                errorText = stringResource(R.string.error_contact_information_required),
                imeAction = ImeAction.Done,
                onKeyboardAction = ::submitFromKeyboard,
                modifier = Modifier.focusRequester(contactFocusRequester)
            )

            Button(
                onClick = vm::submit,
                enabled = !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(stringResource(R.string.action_post_item))
                }
            }
        }
    }
}

@Composable
private fun ImagePickerBox(
    state: AddItemUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val errorColor = MaterialTheme.colorScheme.error
    val borderColor = if (state.imageError) {
        errorColor
    } else {
        MaterialTheme.colorScheme.outline
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                .drawBehind {
                    drawRoundRect(
                        color = borderColor,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                        )
                    )
                }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val imageUri = state.imageUri
            if (imageUri != null) {
                ImagePreview(imageUri = imageUri)
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(27.dp)
                            )
                            .padding(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.image_add_photos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.image_tap_upload),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (state.imageError) {
            Text(
                text = stringResource(R.string.error_photo_required),
                color = errorColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun ImagePreview(
    imageUri: Any,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = imageUri,
            contentDescription = stringResource(R.string.image_selected_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.image_tap_change),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RequiredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholderText: String,
    isError: Boolean,
    errorText: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    imeAction: ImeAction = ImeAction.Next,
    onKeyboardAction: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholderText) },
            isError = isError,
            supportingText = if (isError) {
                { Text(errorText) }
            } else {
                null
            },
            minLines = minLines,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { onKeyboardAction() },
                onDone = { onKeyboardAction() }
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
