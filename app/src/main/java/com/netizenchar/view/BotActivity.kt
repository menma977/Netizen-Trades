package com.netizenchar.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
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
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var payIn: BigDecimal
  private lateinit var payOut: BigDecimal
  private lateinit var profit: BigDecimal
  private lateinit var botValue: JSONObject
  private lateinit var sessionUser: SessionUser

  private lateinit var balance: TextView
  private lateinit var targetBalance: TextView
  private lateinit var currencyBalance: TextView
  private lateinit var stop: Button

  private var fibonacciArray = ArrayList<Int>()
  private var format = DecimalFormat("#")
  private var formatLot = DecimalFormat("#.#########")
  private var forcesStop = false
  private var loseBot = false
  private var fibonacciJump = 0
  private var rowChart = 0
  private var maxChart = 30
  private var targetBalanceValue = BigDecimal(0.5)

  /**
   * todo: 3 jalur finish
   * 1. balance mencapai 5%
   * 2. stop paksa
   *
   * todo: rumus
   * low : 0
   * high : 940000
   * payIn : 0.1% dari balance
   */

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot)

    loading = Loading(this)
    sessionUser = SessionUser(this)
    botValue = JSONObject()
    balanceDoge = intent.getSerializableExtra("balanceDoge").toString().toBigDecimal()
    balance = findViewById(R.id.textViewBalance)
    targetBalance = findViewById(R.id.textViewTargetBalance)
    currencyBalance = findViewById(R.id.textViewCurrentBalance)
    stop = findViewById(R.id.buttonStop)
    anyChartView = findViewById(R.id.chart)
    cartesian = AnyChart.line()
    cartesian.background().fill("#ffffff")
    configChart()
    anyChartView.setChart(cartesian)
    loading.openDialog()

    balance.text = formatLot.format(balanceDoge * BigDecimal(0.00000001))

    stop.setOnClickListener {
      forcesStop = true
      goTo = Intent(this, ResultActivity::class.java)
      finish()
      startActivity(goTo)
    }

    botValue.put("value", formatLot.format(balanceDoge * BigDecimal(0.00000001)))
    set.append(botValue.toString())

    setValueFibonacci()
    botMode()

    loading.closeDialog()
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

  private fun botMode() {
    formatLot.format(balanceDoge * BigDecimal(0.001)).toBigDecimal()
    payIn = balanceDoge * BigDecimal(0.001)
    targetBalance.text = formatLot.format((balanceDoge + (balanceDoge * targetBalanceValue)) * BigDecimal(0.00000001))
    val body = HashMap<String, String>()
    Timer().schedule(1000, 1000) {
      if (forcesStop) {
        this.cancel()
        loading.closeDialog()
      } else {
        if (loseBot) {
          if (fibonacciJump >= (fibonacciArray.size - 1)) {
            fibonacciJump = fibonacciArray.size - 1
          } else {
            fibonacciJump += 3
          }
        } else {
          if (fibonacciJump == 0) {
            fibonacciJump = 0
          } else {
            fibonacciJump -= 1
          }
        }
        body["a"] = "PlaceBet"
        body["s"] = sessionUser.get("sessionCookie")
        body["Low"] = "0"
        body["High"] = "940000"
        body["PayIn"] = format.format((payIn * fibonacciArray[fibonacciJump].toBigDecimal()) * (100000000).toBigDecimal())
        body["ProtocolVersion"] = "2"
        body["ClientSeed"] = format.format((0..99999).random())
        body["Currency"] = "doge"
        response = BotController(body).execute().get()
        runOnUiThread {
          try {
            if (response["code"] == 200) {
              payOut = response.getJSONObject("response")["PayOut"].toString().toBigDecimal()
              profit = payOut - payIn
              loseBot = profit < BigDecimal(0)
              payIn = response.getJSONObject("response")["StartingBalance"].toString().toBigDecimal() + (payOut - payIn)
              currencyBalance.text = formatLot.format((balanceDoge + profit) * BigDecimal(0.00000001))
              if (currencyBalance.text.toString().toBigDecimal() > (balanceDoge + (balanceDoge * targetBalanceValue))) {
                this.cancel()
              }
              if (rowChart >= maxChart) {
                set.remove(0)
              }

              if (rowChart == 2) {
                this.cancel()
              }
              rowChart++
              botValue.put(
                "value",
                formatLot.format((balanceDoge + profit) * BigDecimal(0.00000001))
              )
              set.append(botValue.toString())
            } else {
              Toast.makeText(applicationContext, "Bad Connection", Toast.LENGTH_SHORT).show()
              this.cancel()
            }
          } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Invalid request", Toast.LENGTH_SHORT).show()
            this.cancel()
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
}
