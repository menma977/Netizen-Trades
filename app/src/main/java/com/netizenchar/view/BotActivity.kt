package com.netizenchar.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.TooltipPositionMode
import com.netizenchar.R

class BotActivity : AppCompatActivity() {

  private lateinit var anyChartView: AnyChartView
  private lateinit var cartesian: Cartesian
  private lateinit var set: Set
  private lateinit var contentLoop: LinearLayout
  private lateinit var goTo: Intent

  /**
   * todo: 3 jalur finish
   * 1. balance menpacai target
   * 2. bot sudah lebih dari 1 menit
   * 3. stop paksa
   */

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot)

    contentLoop = findViewById(R.id.linearLayoutContentLoop)
    anyChartView = findViewById(R.id.chart)
    contentLoop.removeAllViews()
    cartesian = AnyChart.line()
    cartesian.background().fill("#ffffff")
    configChart()
    anyChartView.setChart(cartesian)
    setView("PayIn", "PayOut", "Balance", contentLoop)
    setView("00000.00000000", "00000.00000000", "00000.00000000", contentLoop)
    setView("00000.00000000", "00000.00000000", "00000.00000000", contentLoop)
  }

  private fun configChart() {
    cartesian.crosshair().enabled(true)
    cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

    set = Set.instantiate()
    val series1Mapping: Mapping = set.mapAs("{ value: 'value' }")

    val series1: Line = cartesian.line(series1Mapping)
    series1.name("Balance")
    series1.hovered().markers().enabled(true)
    series1.stroke("#fd0001")

    cartesian.legend().enabled(true)
    cartesian.legend().fontSize(13.0)
    cartesian.legend().padding(0.0)
  }

  private fun setView(cIn: String, cOut: String, cResult: String, content: LinearLayout) {
    val styleBody = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )

    val styleText = LinearLayout.LayoutParams(
      0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F
    )

    val body = LinearLayout(this)
    val coinIn = TextView(this)
    val coinOut = TextView(this)
    val coinResponse = TextView(this)

    body.layoutParams = styleBody

    coinIn.text = cIn
    coinOut.text = cOut
    coinResponse.text = cResult

    coinIn.layoutParams = styleText
    coinOut.layoutParams = styleText
    coinResponse.layoutParams = styleText

    coinIn.gravity = Gravity.CENTER
    coinOut.gravity = Gravity.CENTER
    coinResponse.gravity = Gravity.CENTER

    body.addView(coinIn)
    body.addView(coinOut)
    body.addView(coinResponse)
    content.addView(body)
  }
}
