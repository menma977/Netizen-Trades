package com.netizenchar.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.netizenchar.MainActivity
import com.netizenchar.R
import com.netizenchar.config.Loading
import com.netizenchar.config.MD5
import com.netizenchar.config.ValueFormat
import com.netizenchar.controller.WebController
import com.netizenchar.model.SessionUser
import org.json.JSONObject
import java.lang.Exception
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

/**
 * todo:preset Data result
 * if balance akir > balance awal = profit
 * profit = balance akir - balance awal
 * if profit = profit / 3
 * profit(60%) = wallet user
 * profit(20%) = wallet sponsor
 * profit(20%) = wallet IT
 * send api(profit(60%)) : menang.php
 * else
 * send api(balance akir - balance awal) : kalah.php
 */
class ResultActivity : AppCompatActivity() {
  private lateinit var loading: Loading
  private lateinit var user: SessionUser
  private lateinit var valueFormat: ValueFormat

  private lateinit var statusView: TextView
  private lateinit var description: TextView

  private lateinit var uniqueCode: String
  private lateinit var startBalance: BigDecimal
  private lateinit var response: JSONObject
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_result)

    loading = Loading(this)
    user = SessionUser(this)
    valueFormat = ValueFormat()

    loading.openDialog()

    statusView = findViewById(R.id.statusTextView)
    description = findViewById(R.id.walletDescriptionTextView)

    uniqueCode = intent.getSerializableExtra("uniqueCode").toString()
    startBalance = intent.getSerializableExtra("startBalance").toString().toBigDecimal()

    statusView.text = intent.getSerializableExtra("status").toString()
    description.text =
      "We will return your capital and trading profit or the remaining cut loss from your capital to your doge wallet in a few moments."

    sendDataToWeb()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    val goTo = Intent(this, MainActivity::class.java)
    startActivity(goTo)
    finish()
  }

  private fun sendDataToWeb() {
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "EndTrading1"
      body["usertrade"] = user.get("username")
      body["passwordtrade"] = user.get("password")
      body["notrx"] = intent.getSerializableExtra("uniqueCode").toString()
      body["status"] = intent.getSerializableExtra("status").toString()
      body["startbalance"] = valueFormat.decimalToDoge(startBalance).toPlainString()
      body["ref"] = MD5().convert(user.get("username") + user.get("password") + body["notrx"] + body["status"] + "balanceakhirb0d0nk111179")
      response = WebController(body).execute().get()
      try {
        if (response.getInt("code") == 200) {
          runOnUiThread {
            statusView.text = response.getJSONObject("data")["profit"].toString()
            loading.closeDialog()
          }
        } else {
          runOnUiThread {
            statusView.text = response["data"].toString()
            loading.closeDialog()
          }
        }
      } catch (e: Exception) {
        runOnUiThread {
          Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }
}
