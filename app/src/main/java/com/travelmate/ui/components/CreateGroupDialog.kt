package com.travelmate.ui.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.travelmate.ui.theme.ColorPrimary
import com.travelmate.ui.theme.ColorTextPrimary
import com.travelmate.ui.theme.ColorTextSecondary

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, destination: String, description: String, imageUrl: String?) -> Unit,
    onUploadImage: (uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // ✅ LOG pour tracer les changements d'état de l'image
    LaunchedEffect(imageUrl) {
        Log.d("CreateGroupDialog", "📍 imageUrl state changed: $imageUrl")
    }

    LaunchedEffect(isUploading) {
        Log.d("CreateGroupDialog", "📍 isUploading state changed: $isUploading")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("CreateGroupDialog", "=== IMAGE PICKER RESULT ===")
        uri?.let {
            Log.d("CreateGroupDialog", "✅ Image selected from gallery")
            Log.d("CreateGroupDialog", "📍 Image URI: $it")

            selectedImageUri = it
            isUploading = true
            uploadError = null

            Log.d("CreateGroupDialog", "📤 Starting upload process...")

            onUploadImage(
                it,
                { url ->
                    Log.d("CreateGroupDialog", "=== UPLOAD SUCCESS CALLBACK ===")
                    Log.d("CreateGroupDialog", "✅ Upload completed successfully")
                    Log.d("CreateGroupDialog", "📍 Received URL from backend: $url")

                    imageUrl = url
                    isUploading = false
                    uploadError = null

                    Log.d("CreateGroupDialog", "📍 State updated - imageUrl: $imageUrl, isUploading: $isUploading")
                },
                { error ->
                    Log.e("CreateGroupDialog", "=== UPLOAD ERROR CALLBACK ===")
                    Log.e("CreateGroupDialog", "❌ Upload failed")
                    Log.e("CreateGroupDialog", "📍 Error message: $error")

                    isUploading = false
                    uploadError = error
                    selectedImageUri = null

                    Log.d("CreateGroupDialog", "📍 State updated - uploadError: $error, isUploading: $isUploading")
                }
            )
        } ?: run {
            Log.w("CreateGroupDialog", "⚠️ Image picker returned null URI")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Créer un groupe",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fermer", tint = ColorTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Photo du groupe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            color = if (isUploading) ColorPrimary else ColorTextSecondary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = !isUploading) {
                            Log.d("CreateGroupDialog", "📸 Image picker button clicked")
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploading -> {
                            Log.d("CreateGroupDialog", "🔄 Showing upload progress indicator")
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = ColorPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Upload en cours...",
                                    fontSize = 12.sp,
                                    color = ColorTextSecondary
                                )
                            }
                        }
                        selectedImageUri != null -> {
                            Log.d("CreateGroupDialog", "🖼️ Displaying selected image preview: $selectedImageUri")
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Photo du groupe",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = {
                                    Log.e("CreateGroupDialog", "❌ Error loading image preview: ${it.result.throwable.message}")
                                },
                                onSuccess = {
                                    Log.d("CreateGroupDialog", "✅ Image preview loaded successfully")
                                }
                            )
                        }
                        else -> {
                            Log.d("CreateGroupDialog", "📷 Showing placeholder icon")
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = ColorTextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Cliquez pour choisir une photo",
                                    color = ColorTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                if (uploadError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        uploadError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                    Log.e("CreateGroupDialog", "⚠️ Displaying error message: $uploadError")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Nom du groupe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Voyageurs solo en Asie") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Destination",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Thaïlande") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Décrivez votre groupe...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPrimary,
                        unfocusedBorderColor = ColorTextSecondary.copy(alpha = 0.3f)
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && description.isNotBlank()) {
                            Log.d("CreateGroupDialog", "===========================================")
                            Log.d("CreateGroupDialog", "=== CREATE GROUP BUTTON CLICKED ===")
                            Log.d("CreateGroupDialog", "===========================================")
                            Log.d("CreateGroupDialog", "📋 Form Data:")
                            Log.d("CreateGroupDialog", "   • Name: $name")
                            Log.d("CreateGroupDialog", "   • Destination: $destination")
                            Log.d("CreateGroupDialog", "   • Description: $description")
                            Log.d("CreateGroupDialog", "   • ImageUrl: ${imageUrl ?: "NULL - No image uploaded"}")
                            Log.d("CreateGroupDialog", "   • isUploading: $isUploading")
                            Log.d("CreateGroupDialog", "   • selectedImageUri: $selectedImageUri")
                            Log.d("CreateGroupDialog", "===========================================")

                            if (imageUrl != null) {
                                Log.d("CreateGroupDialog", "✅ Image URL is present, will be sent to backend")
                            } else {
                                Log.w("CreateGroupDialog", "⚠️ No image URL - group will be created without image")
                            }

                            onConfirm(name, destination, description, imageUrl)

                            Log.d("CreateGroupDialog", "📤 onConfirm callback called with imageUrl: $imageUrl")
                        } else {
                            Log.w("CreateGroupDialog", "⚠️ Cannot create group - missing required fields")
                            Log.w("CreateGroupDialog", "   • Name blank: ${name.isBlank()}")
                            Log.w("CreateGroupDialog", "   • Description blank: ${description.isBlank()}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && description.isNotBlank() && !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload en cours...")
                    } else {
                        Text(
                            "Créer le groupe",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}