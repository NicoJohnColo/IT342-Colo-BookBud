package com.example.bookbud.features.notifications.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bookbud.R
import com.example.bookbud.shared.auth.TokenManager
import com.example.bookbud.shared.network.NotificationApiClient
import kotlinx.coroutines.*

class NotificationsFragment : Fragment() {
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNotifications(view)
    }

    private fun loadNotifications(view: View) {
        job = lifecycleScope.launch {
            val token = TokenManager.getAccessToken() ?: return@launch
            val notifications = NotificationApiClient.getMyNotifications(token)
            
            withContext(Dispatchers.Main) {
                val container = view.findViewById<LinearLayout>(R.id.notificationsContainer)
                container?.removeAllViews()
                
                notifications.forEach { notif ->
                    val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_notification, container, false)
                    itemView.findViewById<TextView>(R.id.notifMessage)?.text = notif.message
                    itemView.findViewById<TextView>(R.id.notifTime)?.text = notif.createdAt ?: "Now"
                    container?.addView(itemView)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
