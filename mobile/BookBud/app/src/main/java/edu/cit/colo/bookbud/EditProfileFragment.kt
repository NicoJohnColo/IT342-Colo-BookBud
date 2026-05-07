package edu.cit.colo.bookbud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class EditProfileFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: Button

    private lateinit var editUsername: EditText
    private lateinit var editMobile: EditText
    private lateinit var editFacebook: EditText
    private lateinit var editMessenger: EditText

    private var accessToken: String? = null
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressEdit)
        scrollView = view.findViewById(R.id.scrollEdit)
        btnBack = view.findViewById(R.id.btnBack)
        btnSave = view.findViewById(R.id.btnSave)

        editUsername = view.findViewById(R.id.editUsername)
        editMobile = view.findViewById(R.id.editMobile)
        editFacebook = view.findViewById(R.id.editFacebook)
        editMessenger = view.findViewById(R.id.editMessenger)

        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
        accessToken = prefs.getString("access_token", null)
        userId = prefs.getString("user_id", null)

        btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .commit()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val userId = this.userId ?: return@Thread

                val result = UserApiClient.getUserProfile(accessToken, userId)
                val user = result.data as? UserProfileDTO

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE

                    user?.let {
                        editUsername.setText(it.username ?: "")
                        editMobile.setText(it.mobileNumber ?: "")
                        editFacebook.setText(it.facebookUrl ?: "")
                        editMessenger.setText(it.messenger ?: "")
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun saveProfile() {
        val username = editUsername.text.toString().trim()
        val mobile = editMobile.text.toString().trim()
        val facebook = editFacebook.text.toString().trim()
        val messenger = editMessenger.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Username is required", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false

        Thread {
            try {
                val accessToken = this.accessToken ?: return@Thread
                val userId = this.userId ?: return@Thread

                val updateRequest = UpdateUserRequest(
                    username = username,
                    facebookUrl = facebook,
                    messenger = messenger,
                    mobileNumber = mobile
                )

                val result = UserApiClient.updateUserProfile(
                    accessToken,
                    userId,
                    updateRequest
                )

                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true

                    if (result.success) {
                        // Update stored username
                        val prefs = requireContext().getSharedPreferences("bookbud_prefs", 0)
                        prefs.edit().putString("username", username).apply()

                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ProfileFragment())
                            .commit()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
