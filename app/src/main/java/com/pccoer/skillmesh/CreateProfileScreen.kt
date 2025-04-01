package com.pccoer.skillmesh.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import coil3.compose.AsyncImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pccoer.skillmesh.R
import com.pccoer.skillmesh.viewmodel.ProfileResult
import com.pccoer.skillmesh.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateProfileScreen(viewModel: ProfileViewModel, onProfileSaved: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    var location by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val profileResult by viewModel.profileResult.collectAsState()

    val fusedLocationClient: FusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(profileResult) {
        if (profileResult is ProfileResult.Success) {
            Toast.makeText(context, "Profile created successfully!", Toast.LENGTH_SHORT).show()
            onProfileSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // increased spacing
    ) {
        Text(text = "Tell us about yourself", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        ProfilePictureSection(imageUri) { imageUri = it }

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        DateOfBirthSection(context, selectedDate) { date ->
            selectedDate = date
            age = calculateAge(date)
        }

        LocationSection(fusedLocationClient, location) { location = it }

        profileResult?.let {
            if (it is ProfileResult.Error) {
                Text(text = it.message, color = Color.Red)
            }
        }

        Button(
            onClick = {
                viewModel.createProfile(
                    firstName, lastName, selectedDate?.time, location, imageUri, onProfileSaved
                )
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isLoading) "Saving..." else "Save")
        }
    }
}

@Composable
fun ProfilePictureSection(imageUri: Uri?, onImageSelected: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> onImageSelected(uri) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { launcher.launch("image/*") },
                tint = Color.Gray
            )
            Text(text = "Add Profile Picture", color = Color.Gray)
        }
    }
}

@Composable
fun DateOfBirthSection(context: Context, selectedDate: Date?, onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    selectedDate?.let {
        calendar.time = it
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, day)
                onDateSelected(newCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { datePickerDialog.show() },
        horizontalAlignment = Alignment.CenterHorizontally // Center alignment
    ) {
        Text(text = "Date of Birth", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            text = selectedDate?.let { dateFormat.format(it) } ?: "Select Date",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LocationSection(
    fusedLocationClient: FusedLocationProviderClient,
    location: String?,
    onLocationFetched: (String) -> Unit
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocation(fusedLocationClient, context, onLocationFetched)
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Button(
        onClick = {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = location ?: "Fetch Location")
    }
}

fun fetchLocation(fusedLocationClient : FusedLocationProviderClient, context: Context, onLocationFetched: (String) -> Unit){
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
        if (loc != null) {
            val geoCoder = Geocoder(context, Locale.getDefault())
            val addressList = geoCoder.getFromLocation(loc.latitude, loc.longitude, 1)
            if (!addressList.isNullOrEmpty()) {
                onLocationFetched(addressList[0].getAddressLine(0))
            } else {
                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
fun calculateAge(dateOfBirth: Date): Int {
    val dobCalendar = Calendar.getInstance()
    dobCalendar.time = dateOfBirth
    val today = Calendar.getInstance()
    var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}