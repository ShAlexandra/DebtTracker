package com.example.debttracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized color palette for the DebtTracker app.
 * All hardcoded colors across the UI layer live here.
 */
object AppColors {

    // ── Debt type primary colors ──
    /** Green — debt type OWE_ME ("Мне должны") */
    val oweMeGreen = Color(0xFF4CAF50)

    /** Red — debt type I_OWE ("Я должен") */
    val iOweRed = Color(0xFFE53935)

    // ── Amount display colors ──
    /** Light green used for remaining amount and payment amounts */
    val remainingGreen = Color(0xFF59C65A)

    /** Warm red used for remaining amount and destructive actions */
    val remainingRed = Color(0xFFE4564F)

    // ── Chip / badge background colors ──
    /** Dark green background for OWE_ME chip */
    val chipOweMeBackground = Color(0xFF1F3B20)

    /** Dark red background for I_OWE chip */
    val chipIOweBackground = Color(0xFF442323)

    // ── Card background tints (10% opacity on type colors) ──
    val oweMeCardBackground = oweMeGreen.copy(alpha = 0.1f)
    val iOweCardBackground = iOweRed.copy(alpha = 0.1f)

    // ── Progress bar ──
    val progressTrack = Color.Gray

    // ── Loading overlay ──
    val loadingOverlay = Color.Black.copy(alpha = 0.3f)

    // ── Generic ──
    val white = Color.White
}