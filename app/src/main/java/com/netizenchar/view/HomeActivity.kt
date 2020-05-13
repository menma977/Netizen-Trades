package com.netizenchar.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.netizenchar.MainActivity
import com.netizenchar.R
import com.netizenchar.config.Loading
import com.netizenchar.config.MD5
import com.netizenchar.controller.BalanceController
import com.netizenchar.controller.DataWebController
import com.netizenchar.model.SessionUser
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.schedule

class HomeActivity : AppCompatActivity() {
  private lateinit var clipboardManager: ClipboardManager
  private lateinit var clipData: ClipData
  private lateinit var goTo: Intent
  private lateinit var loading: Loading
  private lateinit var sessionUser: SessionUser
  private lateinit var response: JSONObject
  private lateinit var balanceDoge: BigDecimal
  private lateinit var logout: Button

  private lateinit var wallet: TextView
  private lateinit var balance: TextView
  private lateinit var copy: Button
  private lateinit var bot: Button
  private lateinit var refreshBalance: LinearLayout
  private var formatLot = DecimalFormat("#.#########")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    loading = Loading(this)
    sessionUser = SessionUser(this)
    wallet = findViewById(R.id.textViewWallet)
    balance = findViewById(R.id.textViewBalance)
    copy = findViewById(R.id.buttonCopy)
    bot = findViewById(R.id.buttonBotMode)
    logout = findViewById(R.id.logoutButton)
    refreshBalance = findViewById(R.id.linearLayoutRefreshBalance)
    loading.openDialog()

    wallet.text = sessionUser.get("wallet")

    copy.setOnClickListener {
      clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipData = ClipData.newPlainText("Wallet", wallet.text.toString())
      clipboardManager.primaryClip = clipData
    }

    refreshBalance.setOnClickListener {
      getBalance()
    }

    logout.setOnClickListener {
      loading.openDialog()
      sessionUser.clear()
      Timer().schedule(1000) {
        goTo = Intent(applicationContext, MainActivity::class.java)
        startActivity(goTo)
        loading.closeDialog()
        finishAffinity()
      }
    }

    bot.setOnClickListener {
      val uniqueCode = UUID.randomUUID().toString()
      loading.openDialog()
      val body = HashMap<String, String>()
      body["a"] = "StartTrading"
      body["usertrade"] = sessionUser.get("username")
      body["passwordtrade"] = sessionUser.get("password")
      body["notrx"] = uniqueCode
      body["balanceawal"] = formatLot.format(balanceDoge * BigDecimal(0.00000001))
      body["ref"] =
        MD5().convert(sessionUser.get("username") + sessionUser.get("password") + uniqueCode + "balanceawalb0d0nk111179")
      Timer().schedule(100) {
        response = DataWebController.StartTrade(body).execute().get()
        println(response)
        runOnUiThread {
          if (response["code"] == 200) {
            if (response.getJSONObject("response")["Status"] == "0") {
              goTo = Intent(applicationContext, BotActivity::class.java)
              goTo.putExtra("uniqueCode", uniqueCode)
              goTo.putExtra("balanceDoge", balanceDoge)
              loading.closeDialog()
              startActivity(goTo)
            } else {
              Toast.makeText(
                applicationContext,
                "You can't play anymore",
                Toast.LENGTH_LONG
              ).show()
              loading.closeDialog()
            }
          } else {
            Toast.makeText(
              applicationContext,
              "your connection is lost",
              Toast.LENGTH_LONG
            ).show()
            loading.closeDialog()
          }
        }
      }
    }

    getBalance()
  }

  override fun onStart() {
    super.onStart()
    getBalance()
  }

  private fun getBalance() {
    loading.openDialog()
    val body = HashMap<String, String>()
    body["a"] = "GetBalance"
    body["s"] = sessionUser.get("sessionCookie")
    body["Currency"] = "doge"
    body["Referrals"] = "0"
    body["Stats"] = "0"
    Timer().schedule(100) {
      response = BalanceController(body).execute().get()
      runOnUiThread {
        if (response["code"] == 200) {
          balanceDoge = response["response"].toString().toBigDecimal()
          val formatBalance = formatLot.format(balanceDoge * BigDecimal(0.00000001))
          balance.text = "DOGE : $formatBalance"
          loading.closeDialog()
        } else {
          balance.text = "DOGE : ERROR. click here to refresh"
          Toast.makeText(
            applicationContext,
            "your balance is not fully read. Please press the balance to refresh your balance",
            Toast.LENGTH_LONG
          ).show()
          bot.isEnabled = false
          loading.closeDialog()
        }
      }
    }
  }
}
