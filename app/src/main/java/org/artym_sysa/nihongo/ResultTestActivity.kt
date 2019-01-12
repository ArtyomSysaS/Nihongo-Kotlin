package org.artym_sysa.nihongo

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.ListView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class ResultTestActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var chart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_test)

        val correct = intent.extras["correct"] as Int
        val incorrect = intent.extras["incorrect"] as Int

        val history = Gson().fromJson<List<TestRecord>>(intent.extras["history"] as String,
                object : TypeToken<List<TestRecord>>() {}.type)

        val header = LayoutInflater.from(this@ResultTestActivity).inflate(R.layout.result_test_listview_header, null)

        listView = findViewById(R.id.testRecordsListView)
        listView.adapter = TestRecordListAdapter(this, R.layout.result_test_record_recycle_view_layout, history)
        listView.addHeaderView(header)

        chart = header.findViewById(R.id.pieChart)

        val dataSet = PieDataSet(
                listOf(
                        PieEntry(correct.toFloat(), correct.toString()),
                        PieEntry(incorrect.toFloat(), incorrect.toString())
                ),
                ""
        )


        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())

        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 2f;
        dataSet.valueTextSize = 10f;
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.selectionShift = 10f
        dataSet.valueLinePart1OffsetPercentage = 80f;
        dataSet.valueLinePart1Length = 1f;
        dataSet.valueLinePart2Length = 0.9f;
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE;
        dataSet.colors = listOf(
                ColorTemplate.rgb("#6AC259"),
                ColorTemplate.rgb("#F05228")
        )

        chart.data = data
        chart.isDrawHoleEnabled = true
        chart.transparentCircleRadius = 58f
        chart.holeRadius = 58f
        chart.animateXY(1400, 1400)
        chart.setDrawEntryLabels(false)
        chart.legend.position = Legend.LegendPosition.BELOW_CHART_CENTER
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setCenterTextSize(40f)
        chart.centerText = when ((correct * 100) / (correct + incorrect)) {
            100 -> "S"
            in 90..99 -> "A"
            in 80..89 -> "B"
            in 70..79 -> "C"
            in 60..69 -> "D"
            in 0..59 -> "F"
            else -> ""
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
    }
}
