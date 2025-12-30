package com.humblecoders.matricareog.screens.welcomeScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.matricareog.R

@Composable
fun WelcomeScreenThree(
    onSkipClicked: () -> Unit,
    onNextClicked: () -> Unit,
    currentPageIndex: Int
) {
    val pinkColor = Color(0xFFEF5DA8)
    val lightPinkColor = Color(0xFFFFD6E5)
    val backgroundCircleColor1 = Color(0xFFF0F4FF)
    val backgroundCircleColor2 = Color(0xFFFFF0F7)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .systemBarsPadding()
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(backgroundCircleColor1)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 350.dp)
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(backgroundCircleColor2)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 60.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section - Logo (fixed at top)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Matri",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "care",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = pinkColor
                )
            }

            // Middle section - Image and text (centered, takes available space)
            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.welcomescreen03),
                    contentDescription = "Pregnant woman",
                    modifier = Modifier
                        .sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = "Welcome",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 24.dp)
                )

                Text(
                    text = "Welcome to Matricare, your companion through every step of your pregnancy journey.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Bottom section - Dots and buttons (fixed at bottom, always visible)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPageIndex) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentPageIndex) pinkColor else lightPinkColor)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onSkipClicked,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "SKIP",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onNextClicked,
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pinkColor
                        ),
                        shape = RoundedCornerShape(25.dp),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "NEXT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
