package com.example.pulse

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private lateinit var switchHydrationReminder: Switch
    private lateinit var intervalSpinner: Spinner
    private lateinit var btnSaveSettings: Button
    private lateinit var prefsManager: SharedPreferencesManager

    companion object {
        private const val REQUEST_CODE_HYDRATION = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager(requireContext())
        setupViews(view)
        loadSettings()
    }

    private fun setupViews(view: View) {
        switchHydrationReminder = view.findViewById(R.id.switch_hydration_reminder)
        intervalSpinner = view.findViewById(R.id.spinner_interval)
        btnSaveSettings = view.findViewById(R.id.btn_save_settings)

        // Setup interval spinner
        val intervals = arrayOf("30 minutes", "1 hour", "2 hours", "3 hours")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        intervalSpinner.adapter = adapter

        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        val sharedPreferences = requireContext().getSharedPreferences("PulseAppPrefs", Context.MODE_PRIVATE)
        val isReminderEnabled = sharedPreferences.getBoolean("hydration_reminder_enabled", false)
        val intervalIndex = sharedPreferences.getInt("hydration_interval_index", 1) // Default 1 hour

        switchHydrationReminder.isChecked = isReminderEnabled
        intervalSpinner.setSelection(intervalIndex)
    }

    private fun saveSettings() {
        val isEnabled = switchHydrationReminder.isChecked
        val intervalIndex = intervalSpinner.selectedItemPosition
        val intervalMinutes = when (intervalIndex) {
            0 -> 30
            1 -> 60
            2 -> 120
            3 -> 180
            else -> 60
        }

        val sharedPreferences = requireContext().getSharedPreferences("PulseAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("hydration_reminder_enabled", isEnabled)
            .putInt("hydration_interval_index", intervalIndex)
            .putInt("hydration_interval_minutes", intervalMinutes)
            .apply()

        if (isEnabled) {
            scheduleHydrationReminder(intervalMinutes)
            showToast("Hydration reminder set for every $intervalMinutes minutes")
        } else {
            cancelHydrationReminder()
            showToast("Hydration reminder disabled")
        }
    }

    private fun scheduleHydrationReminder(intervalMinutes: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            REQUEST_CODE_HYDRATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalMinutes * 60 * 1000L
        val triggerTime = System.currentTimeMillis() + intervalMillis

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intervalMillis,
            pendingIntent
        )
    }

    private fun cancelHydrationReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            REQUEST_CODE_HYDRATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}