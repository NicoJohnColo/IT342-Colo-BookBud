package com.example.bookbud.features.profile.presentation.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.features.auth.presentation.activity.LoginActivity
import com.example.bookbud.features.wishlist.presentation.fragment.WishlistFragment
import com.example.bookbud.features.payments.presentation.fragment.PaymentsFragment
import com.example.bookbud.features.transactions.presentation.fragment.TransactionsFragment
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.UserApiClient
import kotlinx.coroutines.*

class ProfileFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile(view)
    }

    private fun loadProfile(view: View) {
        val progressProfile = view.findViewById<ProgressBar>(R.id.progressProfile)
        val profileScroll = view.findViewById<ScrollView>(R.id.profileScroll)
        val textProfileUsername = view.findViewById<TextView>(R.id.textProfileUsername)
        val textProfileRating = view.findViewById<TextView>(R.id.textProfileRating)
        val textAccountUsername = view.findViewById<TextView>(R.id.textAccountUsername)
        val textFacebookUrl = view.findViewById<TextView>(R.id.textFacebookUrl)
        val textMessenger = view.findViewById<TextView>(R.id.textMessenger)
        val textMobile = view.findViewById<TextView>(R.id.textMobile)
        val avatarInitial = view.findViewById<TextView>(R.id.avatarInitial)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        progressProfile?.visibility = View.VISIBLE
        profileScroll?.visibility = View.GONE

        job = lifecycleScope.launch {
            val token = TokenManager.getAccessToken() ?: return@launch
            val userId = TokenManager.getUserId() ?: return@launch
            val profile = try {
                UserApiClient.getUserProfile(token, userId)
            } catch (e: Exception) {
                null
            }
            
            withContext(Dispatchers.Main) {
                progressProfile?.visibility = View.GONE
                profileScroll?.visibility = View.VISIBLE

                val username = profile?.username ?: TokenManager.getUsername() ?: "User"
                textProfileUsername?.text = username
                textAccountUsername?.text = username
                avatarInitial?.text = username.take(1).uppercase()

                val rating = profile?.rating ?: 5.0
                textProfileRating?.text = String.format("★".repeat(rating.toInt().coerceAtLeast(1)) + " %.1f", rating)

                textFacebookUrl?.text = profile?.facebookUrl ?: "Not provided"
                textMessenger?.text = profile?.messenger ?: "Not provided"
                textMobile?.text = profile?.mobileNumber ?: "Not provided"
                
                // Sign Out action
                btnLogout?.setOnClickListener {
                    TokenManager.clearAll()
                    Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    activity?.finish()
                }

                // Interactive profile section buttons
                view.findViewById<Button>(R.id.btnMyTransactions)?.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TransactionsFragment())
                        .addToBackStack(null)
                        .commit()
                }

                view.findViewById<Button>(R.id.btnMyWishlist)?.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, WishlistFragment())
                        .addToBackStack(null)
                        .commit()
                }

                view.findViewById<Button>(R.id.btnMyEarnings)?.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, PaymentsFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
