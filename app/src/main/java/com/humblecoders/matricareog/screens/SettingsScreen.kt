package com.humblecoders.matricareog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.clickable

@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onViewTerms: () -> Unit,
    onViewPrivacyPolicy: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEmergencyContactsDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header with proper top padding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Settings content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Medical Disclaimer Section
            SettingsSection(title = "Medical Disclaimer") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🚨 CRITICAL MEDICAL DISCLAIMER",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "AI predictions are for EDUCATIONAL PURPOSES ONLY and are NOT medical diagnoses. This report does NOT replace professional medical advice. Always consult healthcare providers for medical decisions. In emergencies, call 108 (India).",
                            fontSize = 14.sp,
                            color = Color(0xFFD32F2F),
                            lineHeight = 20.sp
                        )
                    }
                }
                
                SettingsItem(
                    icon = Icons.Default.Emergency,
                    title = "Emergency Contacts (India)",
                    subtitle = "Tap to view emergency contact numbers",
                    onClick = { showEmergencyContactsDialog = true }
                )
            }
            
            // Legal & Privacy Section
            SettingsSection(title = "Legal & Privacy") {
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms & Conditions",
                    subtitle = "Read our terms of service",
                    onClick = onViewTerms
                )
                
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Learn about data protection",
                    onClick = onViewPrivacyPolicy
                )
            }
            
            // Account Section
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    onClick = { showLogoutDialog = true },
                    isDestructive = true
                )
                
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete Account & Data",
                    subtitle = "Permanently remove all your data",
                    onClick = { showDeleteAccountDialog = true },
                    isDestructive = true
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer
            Text(
                text = "MatriCare - Pregnancy Health AI",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE91E63),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Your trusted companion for pregnancy health management",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "© 2025 Humble Coders. All rights reserved.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout? You will need to sign in again to access your account.",
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE91E63)
                    )
                ) {
                    Text(
                        text = "Logout",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    text = "Delete Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone and will permanently remove all your data including medical history, reports, and personal information.",
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = "Delete Account",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Emergency Contacts Dialog
    if (showEmergencyContactsDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyContactsDialog = false },
            title = {
                Text(
                    text = "🚨 Emergency Contacts (India)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            },
            text = {
                Column {
                    EmergencyContactItem(
                        title = "Medical Emergency",
                        number = "108",
                        description = "National Emergency Response"
                    )
                    EmergencyContactItem(
                        title = "Women's Helpline",
                        number = "181",
                        description = "24/7 Women's Support"
                    )
                    EmergencyContactItem(
                        title = "Ambulance",
                        number = "102",
                        description = "Emergency Medical Transport"
                    )
                    EmergencyContactItem(
                        title = "Police",
                        number = "100",
                        description = "Law Enforcement"
                    )
                    EmergencyContactItem(
                        title = "Fire Brigade",
                        number = "101",
                        description = "Fire & Rescue Services"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showEmergencyContactsDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE91E63)
                    )
                ) {
                    Text(
                        text = "Close",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F9FA)
            )
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Color(0xFFD32F2F) else Color(0xFFE91E63),
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) Color(0xFFD32F2F) else Color.Black
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
    
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFE0E0E0)
    )
}

@Composable
private fun EmergencyContactItem(
    title: String,
    number: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Text(
            text = number,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F)
        )
    }
    
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFE0E0E0)
    )
}
