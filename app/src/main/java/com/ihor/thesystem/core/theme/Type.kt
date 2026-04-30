package com.ihor.thesystem.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.R

// Провайдер Google Fonts
val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Декларація FontFamily. 
// Якщо Google Fonts не завантажаться, Compose автоматично використає системний шрифт (Default)
val RajdhaniFamily = FontFamily(
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = fontProvider, weight = FontWeight.Bold)
)

val TekoFamily = FontFamily(
    Font(googleFont = GoogleFont("Teko"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Teko"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Teko"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Teko"), fontProvider = fontProvider, weight = FontWeight.Bold)
)

val TheSystemTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = TekoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = TextMuted
    ),
    labelLarge = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = TextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        color = TextSecondary
    )
)
