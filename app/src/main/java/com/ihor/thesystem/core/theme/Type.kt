package com.ihor.thesystem.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.R

val SystemBodyFamily = FontFamily(
    Font(resId = R.font.roboto_variable, weight = FontWeight.Normal),
    Font(resId = R.font.roboto_variable, weight = FontWeight.Medium),
    Font(resId = R.font.roboto_variable, weight = FontWeight.SemiBold),
    Font(resId = R.font.roboto_variable, weight = FontWeight.Bold)
)

val SystemDisplayFamily = FontFamily(
    Font(resId = R.font.roboto_variable, weight = FontWeight.Normal),
    Font(resId = R.font.roboto_variable, weight = FontWeight.Medium),
    Font(resId = R.font.roboto_variable, weight = FontWeight.SemiBold),
    Font(resId = R.font.roboto_variable, weight = FontWeight.Bold)
)

val TheSystemTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SystemDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 44.sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = SystemDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 40.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = SystemDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 36.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = SystemDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = SystemDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 28.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = TextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = TextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = SystemBodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        color = TextSecondary
    )
)
