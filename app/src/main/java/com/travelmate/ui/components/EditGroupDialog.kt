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
import com.travelmate.data.models.Group
import com.travelmate.ui.theme.ColorPrimary
import com.travelmate.ui.theme.ColorTextPrimary
import com.travelmate.ui.theme.ColorTextSecondary

@Composable
fun EditGroupDialog(
    group: Group,
    onDismiss: () -> Unit,
    onConfirm: (name: String, destination: String, description: String, imageUrl: String?) -> Unit,
    onUploadImage: (uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var destination by remember { mutableStateOf(group.destination ?: "") }
    var description by remember { mutableStateOf(group.description) }
    var imageUrl by remember { mutableStateOf(group.image) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // ✅ LOG initial
    LaunchedEffect(Unit) {
        Log.d("EditGroupDialog", "=== EDIT GROUP DIALOG OPENED ===")
        Log.d("EditGroupDialog", "📋 Initial group data:")
        Log.d("EditGroupDialog", "   • Group ID: ${group._id}")
        Log.d("EditGroupDialog", "   • Name: ${group.name}")
        Log.d("EditGroupDialog", "   • Destination: ${group.destination}")
        Log.d("EditGroupDialog", "   • Description: ${group.description}")
        Log.d("EditGroupDialog", "   • Image URL: ${group.image ?: "NULL"}")
    }

    // ✅ LOG pour tracer les changements
    LaunchedEffect(imageUrl) {
        Log.d("EditGroupDialog", "📍 imageUrl state changed: $imageUrl")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("EditGroupDialog", "=== IMAGE PICKER RESULT ===")
        uri?.let {
            Log.d("EditGroupDialog", "✅ New image selected")
            Log.d("EditGroupDialog", "📍 Image URI: $it")

            selectedImageUri = it
            isUploading = true
            uploadError = null

            Log.d("EditGroupDialog", "📤 Starting upload...")

            onUploadImage(
                it,
                { url ->
                    Log.d("EditGroupDialog", "=== UPLOAD SUCCESS ===")
                    Log.d("EditGroupDialog", "✅ New image uploaded")
                    Log.d("EditGroupDialog", "📍 New URL from backend: $url")
                    Log.d("EditGroupDialog", "📍 Old URL was: $imageUrl")

                    imageUrl = url
                    isUploading = false
                    uploadError = null

                    Log.d("EditGroupDialog", "📍 imageUrl updated to: $imageUrl")
                },
                { error ->
                    Log.e("EditGroupDialog", "=== UPLOAD ERROR ===")
                    Log.e("EditGroupDialog", "❌ Upload failed: $error")

                    isUploading = false
                    uploadError = error
                    selectedImageUri = null
                }
            )
        } ?: run {
            Log.w("EditGroupDialog", "⚠️ Image picker returned null")
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
                        "Modifier le groupe",
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
                            Log.d("EditGroupDialog", "📸 Image picker clicked")
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploading -> {
                            Log.d("EditGroupDialog", "🔄 Showing upload progress")
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
                            Log.d("EditGroupDialog", "🖼️ Showing new selected image: $selectedImageUri")
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Photo du groupe",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = {
                                    Log.e("EditGroupDialog", "❌ Error loading preview: ${it.result.throwable.message}")
                                },
                                onSuccess = {
                                    Log.d("EditGroupDialog", "✅ Preview loaded")
                                }
                            )
                        }
                        !imageUrl.isNullOrBlank() -> {
                            Log.d("EditGroupDialog", "🖼️ Showing existing image: $imageUrl")
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Photo du groupe",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = { error ->
                                    Log.e("EditGroupDialog", "❌ Error loading existing image: ${error.result.throwable.message}")
                                    Log.e("EditGroupDialog", "❌ Failed URL: $imageUrl")
                                },
                                onSuccess = {
                                    Log.d("EditGroupDialog", "✅ Existing image loaded successfully")
                                }
                            )
                        }
                        else -> {
                            Log.d("EditGroupDialog", "📷 Showing placeholder (no image)")
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Annuler")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && description.isNotBlank()) {
                                Log.d("EditGroupDialog", "===========================================")
                                Log.d("EditGroupDialog", "=== UPDATE GROUP BUTTON CLICKED ===")
                                Log.d("EditGroupDialog", "===========================================")
                                Log.d("EditGroupDialog", "📋 Updated Data:")
                                Log.d("EditGroupDialog", "   • Group ID: ${group._id}")
                                Log.d("EditGroupDialog", "   • Name: $name")
                                Log.d("EditGroupDialog", "   • Destination: $destination")
                                Log.d("EditGroupDialog", "   • Description: $description")
                                Log.d("EditGroupDialog", "   • ImageUrl: ${imageUrl ?: "NULL"}")
                                Log.d("EditGroupDialog", "   • Original image was: ${group.image}")
                                Log.d("EditGroupDialog", "===========================================")

                                onConfirm(name, destination, description, imageUrl)

                                Log.d("EditGroupDialog", "📤 onConfirm called with imageUrl: $imageUrl")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && description.isNotBlank() && !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enregistrer", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}