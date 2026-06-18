package com.humblecoders.matricareog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.matricareog.util.rememberLiveWeeksPregnant
import com.humblecoders.matricareog.components.DueDatePickerDialog
import com.humblecoders.matricareog.model.User
import com.humblecoders.matricareog.util.PregnancyUtils
import com.humblecoders.matricareog.viewmodels.AuthViewModel

private val PrimaryPink = Color(0xFFE91E63)
private val BackgroundGray = Color(0xFFF8F9FA)
private val TextDark = Color(0xFF1A1A1A)
private val TextMuted = Color(0xFF666666)

private val BLOOD_GROUPS = listOf("A+", "A-", "AB+", "AB-", "O+", "O-")
private val DIET_OPTIONS = listOf("Vegetarian", "Non-Vegetarian")
private val ACTIVITY_OPTIONS = listOf("Low", "Moderate", "Active")
private val SLEEP_OPTIONS = listOf("Less than 6 hours", "6-8 hours", "More than 8 hours")
private val MOOD_OPTIONS = listOf("Happy", "Calm", "Anxious", "Stressed", "Tired")
private val FIRST_PREGNANCY_OPTIONS = listOf("Yes", "No")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit
) {
    val currentUser = authViewModel.currentUser.collectAsState().value ?: User()
    var user by remember(currentUser) {
        mutableStateOf(currentUser.copy(
            fullName = PregnancyUtils.capitalizeName(currentUser.fullName)
        ))
    }
    var editingField by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val weeksInt = rememberLiveWeeksPregnant(user.dueDate, user.weeksPregnant)
0    val trimesterLabel = if (weeksInt > 0) PregnancyUtils.getTrimesterLabel(weeksInt) else "-"

    if (showDatePicker) {
        DueDatePickerDialog(
            currentDueDate = user.dueDate,
            onDismiss = {
                showDatePicker = false
                editingField = null
            },
            onDateSelected = { millis ->
                user = PregnancyUtils.applyDueDateToUser(user, millis)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Profile", fontWeight = FontWeight.Bold, color = TextDark)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray,
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = {
                        authViewModel.updateUserProfile(user.copy(
                            fullName = PregnancyUtils.capitalizeName(user.fullName)
                        ))
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryPink)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Tap the edit icon on any field to update it. Due date and trimester are calculated automatically.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                SectionHeader("Account Info (from signup)")
            }

            item {
                EditableFieldRow(
                    label = "Full Name",
                    value = user.fullName.ifBlank { "Not set" },
                    icon = Icons.Default.Person,
                    isEditing = editingField == "name",
                    onEditClick = { editingField = if (editingField == "name") null else "name" }
                ) {
                    OutlinedTextField(
                        value = user.fullName,
                        onValueChange = { user = user.copy(fullName = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Full Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors()
                    )
                }
            }

            item {
                ReadOnlyFieldRow("Email", user.email.ifBlank { "Not set" }, Icons.Default.Email)
            }

            item {
                EditableFieldRow(
                    label = "Age",
                    value = if (user.age.isNotBlank()) "${user.age} years" else "Not set",
                    icon = Icons.Default.Cake,
                    isEditing = editingField == "age",
                    onEditClick = { editingField = if (editingField == "age") null else "age" }
                ) {
                    OutlinedTextField(
                        value = user.age,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(3)
                            user = user.copy(age = digits)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Age (years)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors()
                    )
                }
            }

            item {
                SectionHeader("Pregnancy Details")
            }

            item {
                EditableFieldRow(
                    label = "Due Date",
                    value = user.dueDate.ifBlank { "Set up your due date" },
                    icon = Icons.Default.CalendarToday,
                    isEditing = editingField == "dueDate",
                    onEditClick = {
                        editingField = "dueDate"
                        showDatePicker = true
                    }
                ) {}
            }

            item {
                ReadOnlyFieldRow(
                    "Weeks Pregnant",
                    if (weeksInt > 0) "$weeksInt (from due date)" else "Set due date first",
                    Icons.Default.ChildCare
                )
            }

            item {
                ReadOnlyFieldRow("Trimester", trimesterLabel, Icons.Default.Timeline)
            }

            item {
                EditableFieldRow(
                    label = "First Pregnancy",
                    value = user.firstPregnancy.ifBlank { "Not set" },
                    icon = Icons.Default.Favorite,
                    isEditing = editingField == "firstPregnancy",
                    onEditClick = { editingField = if (editingField == "firstPregnancy") null else "firstPregnancy" }
                ) {
                    DropdownSelector(
                        options = FIRST_PREGNANCY_OPTIONS,
                        selected = user.firstPregnancy,
                        onSelected = { user = user.copy(firstPregnancy = it) }
                    )
                }
            }

            item {
                SectionHeader("Medical Info")
            }

            item {
                EditableFieldRow(
                    label = "Blood Group",
                    value = user.bloodGroup.ifBlank { "Not set" },
                    icon = Icons.Default.Bloodtype,
                    isEditing = editingField == "bloodGroup",
                    onEditClick = { editingField = if (editingField == "bloodGroup") null else "bloodGroup" }
                ) {
                    DropdownSelector(
                        options = BLOOD_GROUPS,
                        selected = user.bloodGroup,
                        onSelected = { user = user.copy(bloodGroup = it) }
                    )
                }
            }

            item {
                EditableFieldRow(
                    label = "Allergies",
                    value = user.allergies.ifBlank { "None" },
                    icon = Icons.Default.Coronavirus,
                    isEditing = editingField == "allergies",
                    onEditClick = { editingField = if (editingField == "allergies") null else "allergies" }
                ) {
                    OutlinedTextField(
                        value = user.allergies,
                        onValueChange = { user = user.copy(allergies = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Allergies (or None)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors()
                    )
                }
            }

            item {
                EditableFieldRow(
                    label = "Doctor Name",
                    value = user.doctorName.ifBlank { "Not set" },
                    icon = Icons.Default.MedicalServices,
                    isEditing = editingField == "doctorName",
                    onEditClick = { editingField = if (editingField == "doctorName") null else "doctorName" }
                ) {
                    OutlinedTextField(
                        value = user.doctorName,
                        onValueChange = { user = user.copy(doctorName = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Doctor Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors()
                    )
                }
            }

            item {
                SectionHeader("Health & Lifestyle")
            }

            item {
                EditableFieldRow(
                    label = "Diet",
                    value = user.diet.ifBlank { "Not set" },
                    icon = Icons.Default.RestaurantMenu,
                    isEditing = editingField == "diet",
                    onEditClick = { editingField = if (editingField == "diet") null else "diet" }
                ) {
                    DropdownSelector(
                        options = DIET_OPTIONS,
                        selected = user.diet,
                        onSelected = { user = user.copy(diet = it) }
                    )
                }
            }

            item {
                EditableFieldRow(
                    label = "Activity Level",
                    value = user.activityLevel.ifBlank { "Not set" },
                    icon = Icons.Default.DirectionsRun,
                    isEditing = editingField == "activityLevel",
                    onEditClick = { editingField = if (editingField == "activityLevel") null else "activityLevel" }
                ) {
                    DropdownSelector(
                        options = ACTIVITY_OPTIONS,
                        selected = user.activityLevel,
                        onSelected = { user = user.copy(activityLevel = it) }
                    )
                }
            }

            item {
                EditableFieldRow(
                    label = "Sleep",
                    value = user.sleep.ifBlank { "Not set" },
                    icon = Icons.Default.Bedtime,
                    isEditing = editingField == "sleep",
                    onEditClick = { editingField = if (editingField == "sleep") null else "sleep" }
                ) {
                    DropdownSelector(
                        options = SLEEP_OPTIONS,
                        selected = user.sleep,
                        onSelected = { user = user.copy(sleep = it) }
                    )
                }
            }

            item {
                EditableFieldRow(
                    label = "Weight",
                    value = if (user.weight.isNotBlank()) "${user.weight} kg" else "Not set",
                    icon = Icons.Default.MonitorWeight,
                    isEditing = editingField == "weight",
                    onEditClick = { editingField = if (editingField == "weight") null else "weight" }
                ) {
                    WeightPicker(
                        weight = user.weight.toFloatOrNull() ?: 60f,
                        onWeightChange = { user = user.copy(weight = it.toInt().toString()) }
                    )
                }
            }

            item {
                EditableFieldRow(
                    label = "Mood",
                    value = user.mood.ifBlank { "Not set" },
                    icon = Icons.Default.Mood,
                    isEditing = editingField == "mood",
                    onEditClick = { editingField = if (editingField == "mood") null else "mood" }
                ) {
                    DropdownSelector(
                        options = MOOD_OPTIONS,
                        selected = user.mood,
                        onSelected = { user = user.copy(mood = it) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryPink,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun EditableFieldRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    editor: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BackgroundGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PrimaryPink, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, fontSize = 12.sp, color = TextMuted)
                    Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = PrimaryPink
                    )
                }
            }
            if (isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                editor()
            }
        }
    }
}

@Composable
private fun ReadOnlyFieldRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BackgroundGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryPink, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 12.sp, color = TextMuted)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select an option") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = outlinedFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun WeightPicker(weight: Float, onWeightChange: (Float) -> Unit) {
    var sliderWeight by remember(weight) { mutableFloatStateOf(weight.coerceIn(40f, 120f)) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                drawArc(
                    color = Color(0xFFFFEBEE),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = PrimaryPink,
                    startAngle = 135f,
                    sweepAngle = 270f * ((sliderWeight - 40f) / 80f),
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${sliderWeight.toInt()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPink
                )
                Text("kg", fontSize = 12.sp, color = TextMuted)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = sliderWeight,
            onValueChange = {
                sliderWeight = it
                onWeightChange(it)
            },
            valueRange = 40f..120f,
            steps = 79,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryPink,
                activeTrackColor = PrimaryPink,
                inactiveTrackColor = Color(0xFFFFEBEE)
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("40 kg", fontSize = 11.sp, color = TextMuted)
            Text("120 kg", fontSize = 11.sp, color = TextMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryPink,
    cursorColor = PrimaryPink,
    unfocusedBorderColor = Color.LightGray
)
