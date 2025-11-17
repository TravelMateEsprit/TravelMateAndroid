package com.travelmate.ui.user.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInsuranceRequestScreen(
    navController: NavController,
    insuranceId: String,
    viewModel: CreateInsuranceRequestViewModel = hiltViewModel()
) {
    var travelerName by remember { mutableStateOf("") }
    var travelerEmail by remember { mutableStateOf("") }
    var travelerPhone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var passportNumber by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Tunisienne") }
    var destination by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("") }
    var returnDate by remember { mutableStateOf("") }
    var travelPurpose by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state) {
        if (state is CreateRequestState.Success) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demande d'inscription") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Informations du voyageur",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = travelerName,
                onValueChange = { travelerName = it },
                label = { Text("Nom complet *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = travelerEmail,
                onValueChange = { travelerEmail = it },
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            
            OutlinedTextField(
                value = travelerPhone,
                onValueChange = { travelerPhone = it },
                label = { Text("Téléphone *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = { Text("Date de naissance (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("1990-01-15") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = passportNumber,
                onValueChange = { passportNumber = it },
                label = { Text("Numéro de passeport *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = nationality,
                onValueChange = { nationality = it },
                label = { Text("Nationalité *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Divider()
            
            Text(
                text = "Détails du voyage",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = departureDate,
                onValueChange = { departureDate = it },
                label = { Text("Date de départ (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("2024-01-15") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = returnDate,
                onValueChange = { returnDate = it },
                label = { Text("Date de retour (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("2024-01-22") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = travelPurpose,
                onValueChange = { travelPurpose = it },
                label = { Text("Motif du voyage") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tourisme, Affaires, Études...") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message supplémentaire") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            if (state is CreateRequestState.Error) {
                Text(
                    text = (state as CreateRequestState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = {
                    viewModel.createRequest(
                        insuranceId = insuranceId,
                        travelerName = travelerName,
                        travelerEmail = travelerEmail,
                        travelerPhone = travelerPhone,
                        dateOfBirth = dateOfBirth,
                        passportNumber = passportNumber,
                        nationality = nationality,
                        destination = destination,
                        departureDate = departureDate,
                        returnDate = returnDate,
                        travelPurpose = travelPurpose.ifBlank { null },
                        message = message.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is CreateRequestState.Loading &&
                        travelerName.isNotBlank() &&
                        travelerEmail.isNotBlank() &&
                        travelerPhone.isNotBlank() &&
                        dateOfBirth.isNotBlank() &&
                        passportNumber.isNotBlank() &&
                        nationality.isNotBlank() &&
                        destination.isNotBlank() &&
                        departureDate.isNotBlank() &&
                        returnDate.isNotBlank()
            ) {
                if (state is CreateRequestState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Soumettre la demande")
                }
            }
        }
    }
}
