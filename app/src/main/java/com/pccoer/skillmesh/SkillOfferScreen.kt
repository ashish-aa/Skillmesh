package com.pccoer.skillmesh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pccoer.skillmesh.data.SkillCategory
import com.pccoer.skillmesh.viewmodel.SkillOfferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillOfferScreen(
    viewModel: SkillOfferViewModel = viewModel(),
    onSkillOfferSubmitted: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Offer a Skill",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 35.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            containerColor = Color.White
        ) { innerPadding ->
            SkillOfferContent(
                viewModel = viewModel,
                onSkillOfferSubmitted = { showConfirmation = true }, // Show confirmation on success
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                onFailure = { errorMessage = it }
            )
            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { errorMessage = null },
                    title = { Text("Error") },
                    text = { Text(errorMessage!!) },
                    confirmButton = {
                        Button(onClick = { errorMessage = null }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showConfirmation) {
                AlertDialog(
                    onDismissRequest = {
                        showConfirmation = false
                        onSkillOfferSubmitted()
                    },
                    title = { Text("Success") },
                    text = { Text("Skill offer submitted successfully!") },
                    confirmButton = {
                        Button(onClick = {
                            showConfirmation = false
                            onSkillOfferSubmitted()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillOfferContent(
    viewModel: SkillOfferViewModel,
    onSkillOfferSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    onFailure: (String) -> Unit
) {
    val skillOffer = viewModel.skillOffer.value
    val categories = SkillCategory.categories
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "What will you offer?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = skillOffer.title,
            onValueChange = { viewModel.updateSkillOffer(skillOffer.copy(title = it)) },
            label = { Text(text = "Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Text(
            text = "What category does it belong to?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = skillOffer.category,
                onValueChange = { },
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            viewModel.updateSkillOffer(skillOffer.copy(category = selectionOption))
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Give a brief description of your skill.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = skillOffer.description,
            onValueChange = { viewModel.updateSkillOffer(skillOffer.copy(description = it)) },
            label = { Text(text = "Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.submitSkillOffer(
                    onSuccess = { onSkillOfferSubmitted() }, // Notify success
                    onFailure = { onFailure(it) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Next")
        }
    }
}