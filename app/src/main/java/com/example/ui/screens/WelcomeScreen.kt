package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Welcome screen matching user screenshot Screenshot_20261017_215405.
 */
@Composable
fun WelcomeScreen(
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp, vertical = 24.dp)
            .testTag("welcome_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 30.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            val termsText = buildAnnotatedString {
                append("Please read and accept our ")
                withStyle(SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)) {
                    append("Terms")
                }
                append(" and ")
                withStyle(SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)) {
                    append("Privacy Policy")
                }
                append(" which set out the terms of use of our app and explain how we collect, use and process your information, the privacy rights available to you and instructions on how you can exercise such privacy rights, otherwise ")
                withStyle(SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)) {
                    append("see the options")
                }
                append(" available to you.")
            }

            Text(
                text = termsText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF334155),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = onAccept,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .testTag("welcome_accept_button")
            ) {
                Text(
                    text = "Accept",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
