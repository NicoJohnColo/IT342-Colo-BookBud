package com.example.bookbud.shared.extension

import android.util.Patterns

fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= 8 && this.any { it.isDigit() } && this.any { it.isLetter() }
}
