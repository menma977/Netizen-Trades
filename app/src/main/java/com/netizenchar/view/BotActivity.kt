package com.netizenchar.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Mapping
import com.anychart.data.Set
import com.anychart.enums.TooltipPositionMode
import com.netizenchar.R
import com.netizenchar.config.Loading
import com.netizenchar.controller.BotController
import com.netizenchar.model.SessionUser
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class BotActivity : AppCompatActivity() {

  private lateinit var anyChartView: AnyChartView
  private lateinit var cartesian: Cartesian
  private lateinit var set: Set
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
  private lateinit var botValue: JSONObject
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
  private var maxChart = 30
  private var targetBalanceValue = BigDecimal(0.05)

  /**
   * todo: 3 jalur finish
   * 1. balance mencapai 5%
   * 2. stop paksa
   */

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot)

    loading = Loading(this)
    sessionUser = SessionUser(this)
    botValue = JSONObject()
    balanceDoge = intent.getSerializableExtra("balanceDoge").toString().toBigDecimal()
    balance = findViewById(R.id.textViewBalance)
    anyChartView = findViewById(R.id.chart)
    progressBar = findViewById(R.id.progressBar)
    cartesian = AnyChart.line()
    cartesian.background().fill("#ffffff")
    configChart()
    anyChartView.setChart(cartesian)
    loading.openDialog()

    balance.text = formatLot.format(balanceDoge * BigDecimal(0.00000001))
    progressBar.min = 0
    progressBar.progress = 0
    progressBar.max = 100

    setValueFibonacci()
    botMode()

    loading.closeDialog()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    stop = true
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
    cartesian.yAxis(false)
    cartesian.xAxis(false)
  }

  private fun botMode() {
    balanceDogeLocal = balanceDoge
    payIn = (balanceDoge * BigDecimal(0.00000001)) * BigDecimal(0.001)
    val targetBalanceMirror = balanceDoge * BigDecimal(0.00000001)
    balanceTargetDogeLocal = formatLot.format((targetBalanceMirror * targetBalanceValue) + targetBalanceMirror)
    val body = HashMap<String, String>()
    Timer().schedule(1000, 2000) {
      if (stop) {
        this.cancel()
      } else {
        body["a"] = "PlaceBet"
        body["s"] = sessionUser.get("sessionCookie")
        body["Low"] = "0"
        body["High"] = "940000"
        body["PayIn"] = format.format((payIn * fibonacciArray[fibonacciJump].toBigDecimal()) * BigDecimal(1000000000))
        body["ProtocolVersion"] = "2"
        body["ClientSeed"] = format.format((0..99999).random())
        body["Currency"] = "doge"
        response = BotController(body).execute().get()
        runOnUiThread {
          try {
            if (response["code"] == 200) {
              payOut = response.getJSONObject("response")["PayOut"].toString().toBigDecimal()
              balanceDogeLocal = response.getJSONObject("response")["StartingBalance"].toString().toBigDecimal()
              profit = payOut - (payIn * BigDecimal(1000000000))
              loseBot = (profit) < BigDecimal(0)
              payIn = (balanceDogeLocal) * BigDecimal(0.00000001) * BigDecimal(0.001)
              balanceRemainingDogeLocal = formatLot.format((balanceDogeLocal) * BigDecimal(0.00000001))
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
                goTo = Intent(applicationContext, ResultActivity::class.java)
                goTo.putExtra("win", true)
                goTo.putExtra("status", "WIN")
                finish()
                startActivity(goTo)
              }
              if (rowChart >= maxChart) {
                set.remove(0)
              }
              rowChart++
              botValue.put("value", balanceRemainingDogeLocal)
              set.append(botValue.toString())
            } else {
              Toast.makeText(applicationContext, "Bad Connection", Toast.LENGTH_SHORT).show()
              this.cancel()
              goTo = Intent(applicationContext, ResultActivity::class.java)
              goTo.putExtra("win", false)
              goTo.putExtra("status", "lost Connection")
              finish()
              startActivity(goTo)
            }
          } catch (e: Exception) {
            Toast.makeText(applicationContext, "Lost", Toast.LENGTH_SHORT).show()
            this.cancel()
            goTo = Intent(applicationContext, ResultActivity::class.java)
            goTo.putExtra("win", false)
            goTo.putExtra("status", "lost")
            finish()
            startActivity(goTo)
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
