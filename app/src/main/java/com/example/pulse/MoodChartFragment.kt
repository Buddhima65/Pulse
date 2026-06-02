package com.example.pulse

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MoodChartFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mood_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPreferencesManager(requireContext())
        setupChart(view)
        loadMoodData()
    }

    private fun setupChart(view: View) {
        lineChart = view.findViewById(R.id.line_chart)

        // Configure chart appearance
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setDrawGridBackground(false)

        // Configure X axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 7

        // Configure Y axis
        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 10f
        yAxis.granularity = 1f

        lineChart.axisRight.isEnabled = false
    }

    private fun loadMoodData() {
        val moodEntries = prefsManager.loadMoods()
        val last7DaysData = getLast7DaysMoodData(moodEntries)

        if (last7DaysData.any { it.second > 0f }) {
            setupChartData(last7DaysData)
        } else {
            // Show empty state or sample data
            showSampleData()
        }
    }

    private fun getLast7DaysMoodData(moodEntries: List<MoodEntry>): List<Pair<String, Float>> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val result = mutableListOf<Pair<String, Float>>()

        // Get last 7 days including today
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = calendar.time
            val dateString = dateFormat.format(date)

            // Find mood entries for this date
            val dayMoods = moodEntries.filter {
                isSameDay(it.date, date)
            }

            // Calculate average mood score for the day
            val averageMood = calculateAverageMoodScore(dayMoods)
            result.add(dateString to averageMood)
        }

        return result
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun calculateAverageMoodScore(moods: List<MoodEntry>): Float {
        if (moods.isEmpty()) return 0f

        var totalScore = 0f
        moods.forEach { mood ->
            totalScore += when (mood.moodText.toLowerCase(Locale.ROOT)) {
                "happy", "excited", "confident" -> 9f
                "relaxed" -> 7f
                "tired" -> 5f
                "anxious" -> 3f
                "sad", "angry" -> 1f
                else -> 5f // neutral
            }
        }

        return totalScore / moods.size
    }

    private fun setupChartData(data: List<Pair<String, Float>>) {
        val entries = ArrayList<Entry>()
        val dates = ArrayList<String>()

        data.forEachIndexed { index, (date, moodScore) ->
            entries.add(Entry(index.toFloat(), moodScore))
            dates.add(date)
        }

        val dataSet = LineDataSet(entries, "Mood Trend")
        dataSet.color = Color.parseColor("#FF6B73") // Pulse primary color
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 3f
        dataSet.setCircleColor(Color.parseColor("#FF6B73"))
        dataSet.circleRadius = 6f
        dataSet.setDrawCircleHole(true)
        dataSet.setDrawValues(true)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Set X axis labels
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(dates)

        lineChart.invalidate() // refresh chart
    }

    private fun showSampleData() {
        // Show sample data when no mood entries exist
        val sampleData = listOf(
            "Oct 01" to 7f,
            "Oct 02" to 5f,
            "Oct 03" to 8f,
            "Oct 04" to 6f,
            "Oct 05" to 9f,
            "Oct 06" to 4f,
            "Today" to 7f
        )
        setupChartData(sampleData)
    }
}