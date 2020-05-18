package com.netizenchar.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.netizenchar.MainActivity
import com.netizenchar.R
import com.netizenchar.config.Loading
import com.netizenchar.controller.BotController
import com.netizenchar.model.SessionUser
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class BotActivity : AppCompatActivity() {
  private lateinit var cubicLineChart: ValueLineChart
  private lateinit var series: ValueLineSeries
  private lateinit var goTo: Intent
  private lateinit var balanceDoge: BigDecimal
  private lateinit var balanceDogeLocal: BigDecimal
  private lateinit var balanceTargetDogeLocal: String
  private lateinit var balanceRemainingDogeLocal: String
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var payIn: BigDecimal
  private lateinit var payOut: BigDecimal
  private lateinit var profit: BigDecimal
  private lateinit var sessionUser: SessionUser
  private lateinit var balance: TextView
  private lateinit var progressBar: ProgressBar

  private var fibonacciArray = ArrayList<Int>()
  private var format = DecimalFormat("#")
  private var formatLot = DecimalFormat("#.#########")
  private var loseBot = false
  private var stop = false
  private var fibonacciJump = 0
  private var rowChart = 0
  private var targetBalanceValue = BigDecimal(0.05)
  private var uniqueCode = ""

  /**
   * todo: 3 jalur finish
   * 1. balance mencapai 5%
   * 2. stop paksa
   */

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot)

    uniqueCode = intent.getSerializableExtra("uniqueCode").toString()

    cubicLineChart = findViewById(R.id.cubicLineChart)
    series = ValueLineSeries()

    loading = Loading(this)
    sessionUser = SessionUser(this)
    balanceDoge = intent.getSerializableExtra("balanceDoge").toString().toBigDecimal()
    balance = findViewById(R.id.textViewBalance)
    progressBar = findViewById(R.id.progressBar)
    loading.openDialog()

    balance.text = "Start Balance : " + formatLot.format(balanceDoge * BigDecimal(0.00000001))
    progressBar.min = 0
    progressBar.progress = 0
    progressBar.max = 100

    configChart()
    setValueFibonacci()
    botMode()

    loading.closeDialog()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    stop = true
  }

  private fun configChart() {
    series.color = Color.RED
    cubicLineChart.axisTextColor = Color.GRAY
    cubicLineChart.containsPoints()
    cubicLineChart.isUseDynamicScaling = true
    cubicLineChart.addSeries(series)
    cubicLineChart.startAnimation()
  }

  private fun botMode() {
    var dilay: Long = 1000
    balanceDogeLocal = balanceDoge
    payIn = (balanceDoge * BigDecimal(0.00000001)) * BigDecimal(0.01)
    val targetBalanceMirror = balanceDoge * BigDecimal(0.00000001)
    balanceTargetDogeLocal =
      formatLot.format((targetBalanceMirror * targetBalanceValue) + targetBalanceMirror).replace(",", ".")
    val body = HashMap<String, String>()
    Timer().schedule(dilay, 1000) {
      if (stop) {
        this.cancel()
      } else {
        body["a"] = "PlaceBet"
        body["s"] = sessionUser.get("sessionCookie")
        body["Low"] = "0"
        body["High"] = "940000"
        body["PayIn"] = format.format((payIn * fibonacciArray[fibonacciJump].toBigDecimal()) * BigDecimal(100000000))
        body["ProtocolVersion"] = "2"
        body["ClientSeed"] = format.format((0..99999).random())
        body["Currency"] = "doge"
        response = BotController(body).execute().get()
        runOnUiThread {
          try {
            when {
              response["code"] == 200 -> {
                payOut = response.getJSONObject("response")["PayOut"].toString().toBigDecimal()
                balanceDogeLocal = response.getJSONObject("response")["StartingBalance"].toString().toBigDecimal()
                profit = payOut - (payIn * BigDecimal(100000000))
                loseBot = (profit) < BigDecimal(0)
                payIn = (balanceDogeLocal) * BigDecimal(0.00000001) * BigDecimal(0.01)
                balanceRemainingDogeLocal =
                  formatLot.format((balanceDogeLocal) * BigDecimal(0.00000001)).replace(",", ".")
                progress(
                  balanceDoge * BigDecimal(0.00000001),
                  balanceRemainingDogeLocal.toBigDecimal(),
                  balanceTargetDogeLocal.toBigDecimal()
                )
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
                if (balanceRemainingDogeLocal.toBigDecimal() > balanceTargetDogeLocal.toBigDecimal()) {
                  this.cancel()
                  this.cancel()
                  Timer().schedule(2000) {
                    loading.openDialog()
                    runOnUiThread {
                      goTo = Intent(applicationContext, ResultActivity::class.java)
                      goTo.putExtra("status", "WIN")
                      startActivity(goTo)
                      loading.closeDialog()
                      finishAffinity()
                    }
                  }
                } else if (balanceRemainingDogeLocal.toBigDecimal() <= BigDecimal(0)) {
                  this.cancel()
                  Timer().schedule(2000) {
                    runOnUiThread {
                      goTo = Intent(applicationContext, ResultActivity::class.java)
                      goTo.putExtra("status", "LOSS")
                      startActivity(goTo)
                      finishAffinity()
                    }
                  }
                }
                series.addPoint(ValueLinePoint("$rowChart", balanceRemainingDogeLocal.toFloat()))
                cubicLineChart.addSeries(series)
                cubicLineChart.refreshDrawableState()
                rowChart++
                dilay = 1000
              }
              response["code"] == 404 -> {
                Toast.makeText(applicationContext, response["response"].toString(), Toast.LENGTH_LONG).show()
                this.cancel()
                Timer().schedule(2000) {
                  runOnUiThread {
                    goTo = Intent(applicationContext, ResultActivity::class.java)
                    if (balanceRemainingDogeLocal.toBigDecimal() > (balanceDoge * BigDecimal(0.00000001))) {
                      goTo.putExtra("status", "WIN")
                    } else {
                      goTo.putExtra("status", "LOSS")
                    }
                    startActivity(goTo)
                    finishAffinity()
                  }
                }
              }
              else -> {
                Toast.makeText(applicationContext, "Bad Connection 404", Toast.LENGTH_LONG).show()
                this.cancel()
                Timer().schedule(1000) {
                  runOnUiThread {
                    sessionUser.clear()
                    goTo = Intent(applicationContext, MainActivity::class.java)
                    startActivity(goTo)
                    finishAffinity()
                  }
                }
              }
            }
          } catch (e: Exception) {
            runOnUiThread {
              Toast.makeText(
                applicationContext,
                "Bad Connection 500, Wait for the connection to stabilize.",
                Toast.LENGTH_LONG
              ).show()
            }
            dilay = 5000
          }
        }
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
    val startLocal = (start * BigDecimal(100000)).toInt()
    val remainingLocal = (remaining * BigDecimal(100000)).toInt()
    val endLocal = (end * BigDecimal(100000)).toInt()
    progressBar.min = startLocal
    progressBar.max = endLocal
    progressBar.progress = remainingLocal
  }
}
