package com.humblecoders.matricareog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsAndConditionsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Terms & Conditions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
        
        // Terms content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Last Updated: August 24, 2025",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Medical Disclaimer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ CRITICAL MEDICAL DISCLAIMER",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "MatriCare is for informational and educational purposes only and is NOT intended as a substitute for professional medical advice, diagnosis, or treatment.",
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F),
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Terms sections
            Text(
                text = "About These Terms",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "These terms and conditions apply to the MatriCare - Pregnancy Health AI application for Android mobile devices, created by Humble Coders as a Free service. These terms are governed by Indian law and subject to the jurisdiction of Indian courts.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Agreement and Acceptance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Upon downloading, installing, or using the Application, you automatically agree to these terms. You must thoroughly read and understand these terms before using the Application. If you do not agree with any part of these terms, you must not use the Application. You must be at least 18 years of age to use MatriCare.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Health Data and Privacy",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "The Application collects, stores, and processes sensitive health information including personal health metrics, pregnancy history, AI-generated risk assessments, and chatbot interactions. You are responsible for maintaining the security of your device and account credentials.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "AI and Machine Learning Services",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "MatriCare uses artificial intelligence to analyze your health data. AI predictions are provided for educational purposes only and do not constitute medical diagnosis or treatment recommendations. You understand that AI technology has limitations and will not rely solely on AI predictions for medical decisions.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Limitation of Liability",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "To the fullest extent permitted by Indian law, Humble Coders' total liability for any claims related to MatriCare shall not exceed the amount you paid for the Application. We are not liable for medical decisions made based on app information, health complications, or adverse pregnancy outcomes.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Emergency Situations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "MatriCare is not an emergency service. In urgent medical situations, contact emergency services immediately. Emergency Contacts (India): Medical Emergency: 108, Women's Helpline: 181, Ambulance: 102, Police: 100",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "By using MatriCare, you acknowledge that you have read, understood, and agree to these Terms, understand the medical disclaimers and limitations, are 18 years of age or older, and will use the Application responsibly.",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
