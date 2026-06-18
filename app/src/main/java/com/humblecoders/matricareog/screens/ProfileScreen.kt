package com.humblecoders.matricareog.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.matricareog.components.DueDatePickerDialog
import com.humblecoders.matricareog.model.User
import com.humblecoders.matricareog.util.PregnancyUtils
import com.humblecoders.matricareog.util.rememberLiveWeeksPregnant
import androidx.compose.runtime.LaunchedEffect
import com.humblecoders.matricareog.viewmodels.AuthViewModel

private val PrimaryPink = Color(0xFFE91E63)
private val BackgroundGray = Color(0xFFF8F9FA)
private val TextDark = Color(0xFF1A1A1A)
private val TextMuted = Color(0xFF666666)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onPrivacySecurityClick: () -> Unit = {},
    onLanguageAccessibilityClick: () -> Unit = {}
) {
    val currentUser = authViewModel.currentUser.collectAsState().value ?: User()
    var showDueDatePicker by remember { mutableStateOf(false) }
    val userName = PregnancyUtils.capitalizeName(currentUser.fullName).ifBlank { "Your Name" }
    val email = currentUser.email.ifBlank { "-" }
    val age = currentUser.age.ifBlank { "-" }

    val profileFields = listOf(
        currentUser.bloodGroup, currentUser.allergies, currentUser.doctorName,
        currentUser.diet, currentUser.activityLevel, currentUser.sleep,
        currentUser.weight, currentUser.mood, currentUser.dueDate,
        currentUser.weeksPregnant, currentUser.firstPregnancy
    )
    val filledFields = profileFields.count { it.isNotBlank() && it != "-" }
    val completionPercentage = if (profileFields.isNotEmpty()) (filledFields.toFloat() / profileFields.size) * 100 else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ProfileHeaderCard(
                    userName = userName,
                    email = email,
                    age = age,
                    completionPercentage = completionPercentage,
                    onEditClick = onEditProfileClick
                )
            }

            item {
                PregnancyDetailsCard(
                    currentUser = currentUser,
                    authViewModel = authViewModel,
                    onSetUpDueDateClick = { showDueDatePicker = true },
                    onEditDueDateClick = { showDueDatePicker = true }
                )
            }
            item { MedicalInfoCard(currentUser = currentUser) }
            item { HealthLifestyleCard(currentUser = currentUser) }
            item { ActionButtonsSection() }
            item {
                AdditionalOptionsSection(
                    onPrivacyClick = onPrivacySecurityClick,
                    onLanguageClick = onLanguageAccessibilityClick
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showDueDatePicker) {
        DueDatePickerDialog(
            currentDueDate = currentUser.dueDate,
            onDismiss = { showDueDatePicker = false },
            onDateSelected = { millis ->
                authViewModel.updateUserProfile(
                    PregnancyUtils.applyDueDateToUser(currentUser, millis)
                )
            }
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    userName: String,
    email: String,
    age: String,
    completionPercentage: Float,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = PrimaryPink.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (age != "-") "$age Years • $email" else email,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = PrimaryPink.copy(alpha = 0.25f),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile Completion",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = "${completionPercentage.toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPink
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryPink,
                trackColor = Color(0xFFFFEBEE)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PregnancyDetailsCard(
    currentUser: User,
    authViewModel: AuthViewModel,
    onSetUpDueDateClick: () -> Unit,
    onEditDueDateClick: () -> Unit
) {
    val hasDueDate = PregnancyUtils.hasDueDate(currentUser.dueDate)
    val weeksInt = rememberLiveWeeksPregnant(currentUser.dueDate, currentUser.weeksPregnant)
    val weeksPregnant = weeksInt.toFloat()

    LaunchedEffect(weeksInt, currentUser.dueDate) {
        authViewModel.syncPregnancyWeeksIfNeeded()
    }

    val progressColor = Color(PregnancyUtils.getTrimesterColor(weeksInt))
    val trimesterText = if (weeksInt > 0) PregnancyUtils.getTrimesterLabel(weeksInt) else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pregnancy Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                if (hasDueDate) {
                    TextButton(onClick = onEditDueDateClick) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryPink)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = PrimaryPink, fontSize = 13.sp)
                    }
                }
            }

            if (!hasDueDate) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEBEE))
                        .clickable(onClick = onSetUpDueDateClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = PrimaryPink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set up your due date",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink
                        )
                        Text(
                            text = "Tap to choose your expected due date. Weeks and trimester update automatically.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = PrimaryPink
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChildCare, contentDescription = null, tint = progressColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$weeksInt Weeks Pregnant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(progressColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(trimesterText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = progressColor)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEditDueDateClick),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Due Date", fontSize = 12.sp, color = TextMuted)
                        Text(
                            currentUser.dueDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }

                    PregnancyProgressRing(
                        weeksPregnant = weeksPregnant,
                        progressColor = progressColor,
                        modifier = Modifier.size(110.dp)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text("First Pregnancy", fontSize = 12.sp, color = TextMuted)
                        Text(
                            currentUser.firstPregnancy.ifBlank { "-" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PregnancyProgressRing(
    weeksPregnant: Float,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = (weeksPregnant / 40f).coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = Color(0xFFF5F5F5),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (weeksPregnant > 0) {
                Text(
                    text = "${weeksPregnant.toInt()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
                Text("weeks", fontSize = 11.sp, color = TextMuted)
            } else {
                Icon(Icons.Default.ChildCare, contentDescription = null, tint = progressColor, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun MedicalInfoCard(currentUser: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Medical Info", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))
            InfoListItem(Icons.Default.Bloodtype, "Blood Group", currentUser.bloodGroup.ifBlank { "-" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            InfoListItem(Icons.Default.Coronavirus, "Allergies", currentUser.allergies.ifBlank { "-" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            InfoListItem(Icons.Default.MedicalServices, "Doctor Name", currentUser.doctorName.ifBlank { "-" })
        }
    }
}

@Composable
private fun HealthLifestyleCard(currentUser: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Health & Lifestyle", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(16.dp))
            InfoListItem(Icons.Default.RestaurantMenu, "Diet", currentUser.diet.ifBlank { "-" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            InfoListItem(Icons.Default.DirectionsRun, "Activity Level", currentUser.activityLevel.ifBlank { "-" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            InfoListItem(Icons.Default.Bedtime, "Sleep", currentUser.sleep.ifBlank { "-" })
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            InfoListItem(Icons.Default.MonitorWeight, "Weight", if (currentUser.weight.isNotBlank()) "${currentUser.weight} kg" else "-")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
            InfoListItem(Icons.Default.Mood, "Mood", currentUser.mood.ifBlank { "-" })
        }
    }
}

@Composable
private fun InfoListItem(icon: ImageVector, title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).background(BackgroundGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryPink, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, fontSize = 14.sp, color = TextMuted, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
private fun ActionButtonsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f).height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Medical Records", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
            }
        }
        Card(
            modifier = Modifier.weight(1f).height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFEF6C00))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notifications", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF6C00))
            }
        }
    }
}

@Composable
private fun AdditionalOptionsSection(
    onPrivacyClick: () -> Unit,
    onLanguageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            OptionItem(Icons.Default.Lock, "Privacy & Security", onPrivacyClick)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = Color(0xFFF0F0F0))
            OptionItem(Icons.Default.Language, "Language & Accessibility", onLanguageClick)
        }
    }
}

@Composable
private fun OptionItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 15.sp, color = TextDark, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFCCCCCC))
    }
}
