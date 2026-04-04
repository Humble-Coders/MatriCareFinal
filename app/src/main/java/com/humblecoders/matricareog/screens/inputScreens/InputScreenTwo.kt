package com.humblecoders.matricareog.screens.inputScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.matricareog.model.PregnancyHistory
import com.humblecoders.matricareog.viewmodels.MedicalHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreenTwo(
    userId: String,
    onBackPressed: () -> Unit = {},
    onContinuePressed: () -> Unit = {},
    viewModel: MedicalHistoryViewModel
) {
    val personalInfo by viewModel.personalInfo.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val accentColor = Color(0xFFBC8F9F)

    // Form states with real-time validation
    var gravida by remember { mutableStateOf("") }
    var gravidaError by remember { mutableStateOf<String?>(null) }

    var para by remember { mutableStateOf("") }
    var paraError by remember { mutableStateOf<String?>(null) }

    var liveBirths by remember { mutableStateOf("") }
    var liveBirthsError by remember { mutableStateOf<String?>(null) }

    var abortions by remember { mutableStateOf("") }
    var abortionsError by remember { mutableStateOf<String?>(null) }

    var childDeaths by remember { mutableStateOf("") }
    var childDeathsError by remember { mutableStateOf<String?>(null) }

    // Relationship validation errors
    var relationshipError by remember { mutableStateOf<String?>(null) }

    // Real-time validation functions
    fun validateGravida(value: String): String? {
        val intValue = value.toIntOrNull()
        return when {
            value.isEmpty() -> null
            intValue == null -> "Please enter a valid number"
            intValue < 0 -> "Cannot be negative"
            intValue > 20 -> "Too high (maximum: 20)"
            else -> null
        }
    }

    fun validatePara(value: String): String? {
        val intValue = value.toIntOrNull()
        return when {
            value.isEmpty() -> null
            intValue == null -> "Please enter a valid number"
            intValue < 0 -> "Cannot be negative"
            intValue > 15 -> "Too high (maximum: 15)"
            else -> null
        }
    }

    fun validateLiveBirths(value: String): String? {
        val intValue = value.toIntOrNull()
        return when {
            value.isEmpty() -> null
            intValue == null -> "Please enter a valid number"
            intValue < 0 -> "Cannot be negative"
            intValue > 15 -> "Too high (maximum: 15)"
            else -> null
        }
    }

    fun validateAbortions(value: String): String? {
        val intValue = value.toIntOrNull()
        return when {
            value.isEmpty() -> null
            intValue == null -> "Please enter a valid number"
            intValue < 0 -> "Cannot be negative"
            intValue > 10 -> "Too high (maximum: 10)"
            else -> null
        }
    }

    fun validateChildDeaths(value: String): String? {
        val intValue = value.toIntOrNull()
        return when {
            value.isEmpty() -> null
            intValue == null -> "Please enter a valid number"
            intValue < 0 -> "Cannot be negative"
            intValue > 10 -> "Too high (maximum: 10)"
            else -> null
        }
    }

    fun validateRelationships(g: String, p: String, l: String, a: String, d: String): String? {
        val gVal = g.toIntOrNull()
        val pVal = p.toIntOrNull()
        val lVal = l.toIntOrNull()
        val aVal = a.toIntOrNull()
        val dVal = d.toIntOrNull()

        if (gVal != null && pVal != null && aVal != null) {
            if ((pVal + aVal) > gVal) {
                return "Para + Abortions cannot exceed Gravida"
            }
        }

        if (lVal != null && pVal != null && lVal > pVal) {
            return "Live births cannot exceed Para"
        }

        if (dVal != null && lVal != null && dVal > lVal) {
            return "Child deaths cannot exceed live births"
        }

        return null
    }

    // Update relationship validation whenever any field changes
    LaunchedEffect(gravida, para, liveBirths, abortions, childDeaths) {
        relationshipError = validateRelationships(gravida, para, liveBirths, abortions, childDeaths)
    }

    // Check if all fields are valid
    val allFieldsValid = remember(gravida, para, liveBirths, abortions, childDeaths,
        gravidaError, paraError, liveBirthsError, abortionsError,
        childDeathsError, relationshipError) {
        gravida.isNotEmpty() && para.isNotEmpty() && liveBirths.isNotEmpty() &&
                abortions.isNotEmpty() && childDeaths.isNotEmpty() &&
                gravidaError == null && paraError == null && liveBirthsError == null &&
                abortionsError == null && childDeathsError == null && relationshipError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pregnancy History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Progress indicator
                Row(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFFFD6E5), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accentColor, RoundedCornerShape(4.dp))
                    )
                }


                // Form content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Enter Your Pregnancy History",
                            color = Color(0xFF5C4A52),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Real-time validation with relationship checks",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Medical definitions card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Medical Definitions:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "• Gravida (G): Total pregnancies including current\n" +
                                            "• Para (P): Deliveries after 20 weeks gestation\n" +
                                            "• Live Births (L): Total living children\n" +
                                            "• Abortions (A): Pregnancy terminations\n" +
                                            "• Deaths (D): Number of children who died",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Show relationship error if exists
                    if (relationshipError != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ $relationshipError",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Gravida Field
                    item {
                        RealTimeValidatedTextField(
                            value = gravida,
                            onValueChange = {
                                gravida = it
                                gravidaError = validateGravida(it)
                            },
                            label = "Gravida (G)",
                            placeholder = "Total pregnancies (0-20)",
                            keyboardType = KeyboardType.Number,
                            isError = gravidaError != null,
                            errorMessage = gravidaError,
                            helperText = "Range: 0-20",
                            accentColor = accentColor,
                            allowZero = true,
                            guidanceText = "Gravida is the total number of pregnancies you have had, including the current one. Stillbirths and miscarriages after a positive pregnancy test usually count; confirm with your doctor if unsure."
                        )
                    }

                    // Para Field
                    item {
                        RealTimeValidatedTextField(
                            value = para,
                            onValueChange = {
                                para = it
                                paraError = validatePara(it)
                            },
                            label = "Para (P)",
                            placeholder = "Deliveries after 20 weeks (0-15)",
                            keyboardType = KeyboardType.Number,
                            isError = paraError != null,
                            errorMessage = paraError,
                            helperText = "Range: 0-15",
                            accentColor = accentColor,
                            allowZero = true,
                            guidanceText = "Para counts births at or after about 20 weeks of pregnancy (viable deliveries). It does not include early losses before that threshold unless your provider defines it differently."
                        )
                    }

                    // Live Births Field
                    item {
                        RealTimeValidatedTextField(
                            value = liveBirths,
                            onValueChange = {
                                liveBirths = it
                                liveBirthsError = validateLiveBirths(it)
                            },
                            label = "Live Births (L)",
                            placeholder = "Living children (0-15)",
                            keyboardType = KeyboardType.Number,
                            isError = liveBirthsError != null,
                            errorMessage = liveBirthsError,
                            helperText = "Range: 0-15",
                            accentColor = accentColor,
                            allowZero = true,
                            guidanceText = "The number of living children you have. Use the count your doctor uses for your chart (including from prior relationships if relevant to your care)."
                        )
                    }

                    // Abortions Field
                    item {
                        RealTimeValidatedTextField(
                            value = abortions,
                            onValueChange = {
                                abortions = it
                                abortionsError = validateAbortions(it)
                            },
                            label = "Abortions (A)",
                            placeholder = "Pregnancy terminations (0-10)",
                            keyboardType = KeyboardType.Number,
                            isError = abortionsError != null,
                            errorMessage = abortionsError,
                            helperText = "Range: 0-10",
                            accentColor = accentColor,
                            allowZero = true,
                            guidanceText = "Pregnancy losses or terminations as recorded in your history. If you are unsure what to include, use your hospital discharge summary or ask your obstetrician."
                        )
                    }

                    // Child Deaths Field
                    item {
                        RealTimeValidatedTextField(
                            value = childDeaths,
                            onValueChange = {
                                childDeaths = it
                                childDeathsError = validateChildDeaths(it)
                            },
                            label = "Child Deaths (D)",
                            placeholder = "Children who died (0-10)",
                            keyboardType = KeyboardType.Number,
                            isError = childDeathsError != null,
                            errorMessage = childDeathsError,
                            helperText = "Range: 0-10",
                            accentColor = accentColor,
                            allowZero = true,
                            guidanceText = "Children who died after birth. This helps your care team understand your history; enter 0 if this does not apply."
                        )
                    }

                    // Save & Continue Button
                    item {
                        Button(
                            onClick = {
                                val pregnancyHistoryObject = PregnancyHistory(
                                    gravida = gravida.toInt(),
                                    para = para.toInt(),
                                    liveBirths = liveBirths.toInt(),
                                    abortions = abortions.toInt(),
                                    childDeaths = childDeaths.toInt()
                                )

                                viewModel.updatePregnancyHistory(pregnancyHistoryObject)

                                if (personalInfo != null) {
                                    viewModel.storeMedicalHistoryInLiveData(
                                        personalInfo = personalInfo!!,
                                        pregnancyHistory = pregnancyHistoryObject
                                    )
                                    onContinuePressed()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (allFieldsValid) accentColor else Color(0xFFBDBDBD)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = allFieldsValid && !isLoading && personalInfo != null
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Continue to Risk Analysis",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    accentColor: Color,
    allowZero: Boolean = false,
    guidanceText: String? = null,
    modifier: Modifier = Modifier
) {
    val softError = Color(0xFFE57373)
    val softValidBorder = Color(0xFFA5D6A7)
    val softValidIcon = Color(0xFF66BB6A)
    val neutralBorder = Color(0xFFE0E0E0)

    var showGuidance by remember(label) { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = when {
                    isError -> softError.copy(alpha = 0.85f)
                    value.isNotEmpty() && !isError -> softValidBorder.copy(alpha = 0.75f)
                    else -> neutralBorder
                },
                focusedBorderColor = when {
                    isError -> softError
                    value.isNotEmpty() && !isError -> softValidBorder
                    else -> accentColor.copy(alpha = 0.85f)
                },
                focusedLabelColor = when {
                    isError -> softError
                    value.isNotEmpty() && !isError -> softValidIcon
                    else -> accentColor.copy(alpha = 0.9f)
                },
                unfocusedLabelColor = when {
                    isError -> softError
                    value.isNotEmpty() && !isError -> Color(0xFF757575)
                    else -> Color(0xFF9E9E9E)
                },
                errorBorderColor = softError,
                errorLabelColor = softError
            ),
            singleLine = true,
            isError = isError,
            trailingIcon = {
                when {
                    isError -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = softError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    value.isNotEmpty() && !isError -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Valid",
                            tint = softValidIcon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    guidanceText != null -> {
                        IconButton(onClick = { showGuidance = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Field guidance",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        )

        if (showGuidance && guidanceText != null) {
            AlertDialog(
                onDismissRequest = { showGuidance = false },
                title = { Text(label, fontWeight = FontWeight.SemiBold) },
                text = { Text(guidanceText) },
                confirmButton = {
                    TextButton(onClick = { showGuidance = false }) {
                        Text("Got it")
                    }
                }
            )
        }

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = softError,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        // Helper text (only show when no error)
        if (!isError && helperText != null) {
            Text(
                text = helperText,
                color = Color(0xFF757575),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}