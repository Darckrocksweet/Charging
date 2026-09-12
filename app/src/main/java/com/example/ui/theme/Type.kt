package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Modern typography hierarchy for EV Cockpit style
val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Black,
    fontSize = 48.sp,
    lineHeight = 54.sp,
    letterSpacing = (-1.5).sp
  ),
  displayMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 36.sp,
    lineHeight = 42.sp,
    letterSpacing = (-1.0).sp
  ),
  headlineLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.5).sp
  ),
  headlineMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.2).sp
  ),
  titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 19.sp,
    lineHeight = 25.sp,
    letterSpacing = 0.sp
  ),
  titleMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.1.sp
  ),
  titleSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.2.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.2.sp
  ),
  bodySmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp
  ),
  labelLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.4.sp
  ),
  labelMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.6.sp
  ),
  labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.6.sp
  )
)
