package com.netizenchar.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.netizenchar.R
import com.netizenchar.config.Loading
import com.netizenchar.config.ValueFormat
import com.netizenchar.controller.DogeController
import com.netizenchar.model.SessionUser
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.collections.HashMap

class BotActivity : AppCompatActivity() {
  private lateinit var cubicLineChart: ValueLineChart
  private lateinit var series: ValueLineSeries
  private lateinit var goTo: Intent
  private lateinit var progressBar: ProgressBar
  private lateinit var user: SessionUser
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var valueFormat: ValueFormat

  private lateinit var balance: BigDecimal
  private lateinit var balanceTarget: BigDecimal
  private lateinit var balanceRemaining: BigDecimal
  private lateinit var payIn: BigDecimal
  private lateinit var payOut: BigDecimal
  private lateinit var profit: BigDecimal

  private lateinit var balanceView: TextView

  private lateinit var uniqueCode: String

  private var rowChart = 0
  private var loseBot = false
  private var balanceLimitTarget = BigDecimal(0.06)
  private var balanceLimitTargetLow = BigDecimal(0)
  private var seed = (0..99999).random().toString()
  private var thread = Thread()
  private var fibonacciArray = ArrayList<Int>()
  private var fibonacciJump = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot)

    loading = Loading(this)
    user = SessionUser(this)
    valueFormat = ValueFormat()

    uniqueCode = intent.getSerializableExtra("uniqueCode").toString()

    balanceView = findViewById(R.id.textViewBalance)
    progressBar = findViewById(R.id.progressBar)
    cubicLineChart = findViewById(R.id.cubicLineChart)
    series = ValueLineSeries()

    cubicLineChart = findViewById(R.id.cubicLineChart)
    series = ValueLineSeries()

    loading.openDialog()
    balance = intent.getSerializableExtra("balanceDoge").toString().toBigDecimal()
    balanceLimitTarget = intent.getSerializableExtra("target") as BigDecimal
    balanceLimitTargetLow = BigDecimal(0).multiply(BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_HALF_DOWN)
    balanceRemaining = balance
    balanceTarget = valueFormat.dogeToDecimal(valueFormat.decimalToDoge((balance * balanceLimitTarget) + balance))
    payIn = valueFormat.dogeToDecimal(valueFormat.decimalToDoge(balance) * BigDecimal(0.001))
    balanceLimitTargetLow = valueFormat.dogeToDecimal(valueFormat.decimalToDoge(balance) * balanceLimitTargetLow)

    balanceView.text = valueFormat.decimalToDoge(balance).toPlainString()

    progress(balance, balanceRemaining, balanceTarget)
    configChart()
    setValueFibonacci()
    loading.closeDialog()
    thread = Thread() {
      onBotMode()
    }
    thread.start()
  }

  override fun onBackPressed() {
    Toast.makeText(this, "Cannot Return When playing a bot", Toast.LENGTH_LONG).show()
  }

  private fun configChart() {
    series.color = getColor(R.color.colorAccent)
    cubicLineChart.axisTextColor = getColor(R.color.textPrimary)
    cubicLineChart.containsPoints()
    cubicLineChart.isUseDynamicScaling = true
    cubicLineChart.addSeries(series)
    cubicLineChart.startAnimation()
  }

  private fun onBotMode() {
    var time = System.currentTimeMillis()
    val trigger = Object()
    synchronized(trigger) {
      while (balanceRemaining in balanceLimitTargetLow..balanceTarget) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 1000) {
          time = System.currentTimeMillis()
          payIn *= fibonacciArray[fibonacciJump].toBigDecimal()
          val body = HashMap<String, String>()
          body["a"] = "PlaceBet"
          body["s"] = user.get("sessionCookie")
          body["Low"] = "0"
          body["High"] = "899999"
          body["PayIn"] = payIn.toPlainString()
          body["ProtocolVersion"] = "2"
          body["ClientSeed"] = seed
          body["Currency"] = "doge"
          response = DogeController(body).execute().get()
          try {
            if (response["code"] == 200) {
              balanceView.text = valueFormat.decimalToDoge(balance).toPlainString()

              seed = response.getJSONObject("data")["Next"].toString()
              payOut = response.getJSONObject("data")["PayOut"].toString().toBigDecimal()
              balanceRemaining = response.getJSONObject("data")["StartingBalance"].toString().toBigDecimal()
              profit = payOut - payIn
              balanceRemaining += profit
              loseBot = profit < BigDecimal(0)
              payIn = valueFormat.dogeToDecimal(valueFormat.decimalToDoge(balance) * BigDecimal(0.001))

              if (loseBot) {
                if (fibonacciJump >= (fibonacciArray.size - 1)) {
                  fibonacciJump = fibonacciArray.size - 1
                } else {
                  fibonacciJump += 4
                }
              } else {
                if (fibonacciJump == 0) {
                  fibonacciJump = 0
                } else {
                  fibonacciJump -= 1
                }
              }

              runOnUiThread {
                progress(balance, balanceRemaining, balanceTarget)
                if (rowChart >= 49) {
                  series.series.removeAt(0)
                  series.addPoint(ValueLinePoint("$rowChart", valueFormat.decimalToDoge(balanceRemaining).toFloat()))
                } else {
                  series.addPoint(ValueLinePoint("$rowChart", valueFormat.decimalToDoge(balanceRemaining).toFloat()))
                }
                cubicLineChart.addSeries(series)
                cubicLineChart.refreshDrawableState()
              }
              rowChart++
            } else if (response["code"] == 500) {
              runOnUiThread {
                balanceView.text = "sleep mode Active"
                Toast.makeText(applicationContext, "sleep mode Active Wait to continue", Toast.LENGTH_LONG).show()
              }
              trigger.wait(60000)
            } else {
              break
            }
          } catch (e: Exception) {
            break
          }
        }
      }

      goTo = Intent(applicationContext, ResultActivity::class.java)
      if (balanceRemaining >= balanceTarget) {
        goTo.putExtra("status", "WIN")
      } else {
        goTo.putExtra("status", "CUT LOSS")
      }
      goTo.putExtra("startBalance", balance)
      goTo.putExtra("balanceRemaining", balanceRemaining)
      goTo.putExtra("uniqueCode", intent.getSerializableExtra("uniqueCode").toString())
      runOnUiThread {
        startActivity(goTo)
        finish()
      }
    }
  }

  private fun setValueFibonacci() {
    fibonacciArray.add(1)
    fibonacciArray.add(1)
    fibonacciArray.add(2)
    fibonacciArray.add(3)
    fibonacciArray.add(5)
    fibonacciArray.add(8)
    fibonacciArray.add(13)
    fibonacciArray.add(21)
    fibonacciArray.add(34)
    fibonacciArray.add(55)
    fibonacciArray.add(89)
    fibonacciArray.add(144)
    fibonacciArray.add(233)
    fibonacciArray.add(377)
    fibonacciArray.add(610)
    fibonacciArray.add(987)
    fibonacciArray.add(1597)
    fibonacciArray.add(2584)
    fibonacciArray.add(4181)
    fibonacciArray.add(6765)
    fibonacciArray.add(10946)
    fibonacciArray.add(17711)
    fibonacciArray.add(28657)
    fibonacciArray.add(46368)
    fibonacciArray.add(75025)
    fibonacciArray.add(121393)
    fibonacciArray.add(196418)
    fibonacciArray.add(317811)
    fibonacciArray.add(514229)
  }

  private fun progress(start: BigDecimal, remaining: BigDecimal, end: BigDecimal) {
    progressBar.min = valueFormat.decimalToDoge(start).setScale(0, BigDecimal.ROUND_HALF_DOWN).toPlainString().toInt()
    progressBar.progress = valueFormat.decimalToDoge(remaining).setScale(0, BigDecimal.ROUND_HALF_DOWN).toPlainString().toInt()
    progressBar.max = valueFormat.decimalToDoge(end).setScale(0, BigDecimal.ROUND_HALF_DOWN).toPlainString().toInt()
  }
}
