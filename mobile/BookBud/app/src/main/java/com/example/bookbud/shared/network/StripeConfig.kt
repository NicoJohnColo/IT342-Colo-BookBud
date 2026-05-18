package com.example.bookbud.shared.network

/**
 * Configuration for Stripe payment processing
 * The publishable key should be provided at runtime or read from BuildConfig
 */
object StripeConfig {
    // TODO: Move this to BuildConfig or environment variables in production
    // This is the test publishable key - replace with your live key in production
    const val PUBLISHABLE_KEY = "pk_test_YOUR_TEST_KEY_HERE"
    
    // Stripe API version
    const val API_VERSION = "2023-10-16"
}
