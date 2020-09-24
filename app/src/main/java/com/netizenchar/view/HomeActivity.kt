package com.netizenchar.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.netizenchar.MainActivity
import com.netizenchar.R
import com.netizenchar.config.Loading
import com.netizenchar.config.MD5
import com.netizenchar.config.ValueFormat
import com.netizenchar.controller.DogeController
import com.netizenchar.controller.WebController
import com.netizenchar.model.SessionUser
import org.json.JSONObject
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import kotlin.concurrent.schedule


class HomeActivity : AppCompatActivity() {
  private lateinit var clipboardManager: ClipboardManager
  private lateinit var clipData: ClipData
  private lateinit var goTo: Intent
  private lateinit var loading: Loading
  private lateinit var sessionUser: SessionUser
  private lateinit var response: JSONObject
  private lateinit var balanceValue: BigDecimal
  private lateinit var logout: Button
  private lateinit var email: TextView
  private lateinit var wallet: TextView
  private lateinit var balance: TextView
  private lateinit var maxDeposit: TextView
  private lateinit var copy: Button
  private lateinit var botFibonacci: Button
  private lateinit var botProbability: Button
  private lateinit var botWeb: Button
  private lateinit var refreshBalance: LinearLayout
  private lateinit var callWhatsApp: FloatingActionButton

  private lateinit var uniqueCode: String

  private val body = HashMap<String, String>()
  private var limitDepositDefault = BigDecimal(0.000000000, MathContext.DECIMAL32).setScale(8, BigDecimal.ROUND_HALF_DOWN)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    loading = Loading(this)
    sessionUser = SessionUser(this)
    email = findViewById(R.id.textViewEmail)
    wallet = findViewById(R.id.textViewWallet)
    balance = findViewById(R.id.textViewBalance)
    maxDeposit = findViewById(R.id.textViewMaxDeposit)
    copy = findViewById(R.id.buttonCopy)
    botFibonacci = findViewById(R.id.buttonBotFibonacci)
    botProbability = findViewById(R.id.buttonBotModeProbability)
    botWeb = findViewById(R.id.buttonBotWeb)
    logout = findViewById(R.id.logoutButton)
    refreshBalance = findViewById(R.id.linearLayoutRefreshBalance)
    callWhatsApp = findViewById(R.id.floatingActionButtonCall)
    loading.openDialog()

    email.text = sessionUser.get("usernameWeb")
    wallet.text = sessionUser.get("wallet")


    copy.setOnClickListener {
      clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipData = ClipData.newPlainText("Wallet", wallet.text.toString())
      clipboardManager.setPrimaryClip(clipData)
      Toast.makeText(
        applicationContext,
        "Doge wallet has been copied",
        Toast.LENGTH_LONG
      ).show()
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

    botWeb.setOnClickListener {
      uniqueCode = UUID.randomUUID().toString()
      loading.openDialog()
      body["a"] = "StartTradingWeb"
      body["usertrade"] = sessionUser.get("username")
      body["passwordtrade"] = sessionUser.get("password")
      body["notrx"] = uniqueCode
      body["balanceawal"] = ValueFormat().decimalToDoge(balanceValue).toPlainString()
      body["ref"] = MD5().convert(sessionUser.get("username") + sessionUser.get("password") + uniqueCode + "balanceawalb0d0nk111179")
      Timer().schedule(1000) {
        response = WebController(body).execute().get()
        try {
          when {
            response.getJSONObject("data")["Status"] == "0" -> {
              runOnUiThread {
                goTo = Intent(applicationContext, BotWebActivity::class.java)
                goTo.putExtra("profit", response.getJSONObject("data")["profit"].toString())
                startActivity(goTo)
                finish()
                loading.closeDialog()
              }
            }
            response.getJSONObject("data")["Status"] == "2" -> {
              runOnUiThread {
                Toast.makeText(applicationContext, "Our market is busy, please try again in a few moments", Toast.LENGTH_SHORT).show()
                loading.closeDialog()
              }
            }
            else -> {
              runOnUiThread {
                Toast.makeText(applicationContext, "One day trading is only allowed once", Toast.LENGTH_SHORT).show()
                loading.closeDialog()
              }
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

    botFibonacci.setOnClickListener {
      uniqueCode = UUID.randomUUID().toString()
      loading.openDialog()
      if (ValueFormat().decimalToDoge(balanceValue) < BigDecimal(10000)) {
        Toast.makeText(
          applicationContext,
          "Your Doge Balance must more then 10000",
          Toast.LENGTH_LONG
        ).show()
        loading.closeDialog()
      } else {
        body["a"] = "StartTrading1"
        body["usertrade"] = sessionUser.get("username")
        body["passwordtrade"] = sessionUser.get("password")
        body["notrx"] = uniqueCode
        body["balanceawal"] = ValueFormat().decimalToDoge(balanceValue).toPlainString()
        body["ref"] = MD5().convert(sessionUser.get("username") + sessionUser.get("password") + uniqueCode + "balanceawalb0d0nk111179")
        Timer().schedule(100) {
          response = WebController(body).execute().get()
          try {
            if (response["code"] == 200) {
              runOnUiThread {
                goTo = Intent(applicationContext, BotActivity::class.java)
                goTo.putExtra("uniqueCode", uniqueCode)
                goTo.putExtra("balanceDoge", balanceValue)
                goTo.putExtra("target", response.getJSONObject("data").getDouble("persen").div(100).toBigDecimal())
                loading.closeDialog()
                startActivity(goTo)
              }
            } else {
              runOnUiThread {
                Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
                loading.closeDialog()
              }
            }
          } catch (e: Exception) {
            runOnUiThread {
              loading.closeDialog()
              Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }

    botProbability.setOnClickListener {
      val uniqueCode = UUID.randomUUID().toString()
      loading.openDialog()
      if (ValueFormat().decimalToDoge(balanceValue) < BigDecimal(10000)) {
        Toast.makeText(
          applicationContext,
          "Your Doge Balance must more then 10000",
          Toast.LENGTH_LONG
        ).show()
        loading.closeDialog()
      } else {
        body["a"] = "StartTrading2"
        body["usertrade"] = sessionUser.get("username")
        body["passwordtrade"] = sessionUser.get("password")
        body["notrx"] = uniqueCode
        body["balanceawal"] = ValueFormat().decimalToDoge(balanceValue).toPlainString()
        body["ref"] = MD5().convert(sessionUser.get("username") + sessionUser.get("password") + uniqueCode + "balanceawalb0d0nk111179")
        Timer().schedule(100) {
          try {
            response = WebController(body).execute().get()
            if (response["code"] == 200) {
              runOnUiThread {
                goTo = Intent(applicationContext, Bot2Activity::class.java)
                goTo.putExtra("uniqueCode", uniqueCode)
                goTo.putExtra("balanceDoge", balanceValue)
                goTo.putExtra("target", response.getJSONObject("data").getDouble("persen").div(100).toBigDecimal())
                loading.closeDialog()
                startActivity(goTo)
              }
            } else {
              runOnUiThread {
                Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
                loading.closeDialog()
              }
            }
          } catch (e: Exception) {
            runOnUiThread {
              loading.closeDialog()
              Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }

    callWhatsApp.setOnClickListener {
      try {
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=+6281232459859&text=Hai Admin ")
        val waIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(waIntent)
      } catch (e: Exception) {
        println(e.message)
        Toast.makeText(this, "WhatsApp not Installed . ${e.message}", Toast.LENGTH_LONG).show()
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
      response = DogeController(body).execute().get()
      if (response["code"] == 200) {
        balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
        val balanceLimit = if (sessionUser.get("limitDeposit").isEmpty()) {
          ValueFormat().dogeToDecimal(limitDepositDefault)
        } else {
          ValueFormat().dogeToDecimal(sessionUser.get("limitDeposit").toBigDecimal())
        }
        maxDeposit.text = "Maximum : ${ValueFormat().decimalToDoge(balanceLimit)} DOGE"

        if (ValueFormat().decimalToDoge(balanceValue) >= BigDecimal(10000) && balanceValue <= balanceLimit) {
          runOnUiThread {
            balance.text = "Balance : ${ValueFormat().decimalToDoge(balanceValue).toPlainString()} DOGE"
            botFibonacci.visibility = Button.VISIBLE
            botProbability.visibility = Button.VISIBLE
            botWeb.visibility = Button.VISIBLE

            loading.closeDialog()
          }
        } else if (balanceValue > balanceLimit) {
          runOnUiThread {
            balance.text = "Balance : ${ValueFormat().decimalToDoge(balanceValue).toPlainString()} DOGE too High"
            botFibonacci.visibility = Button.GONE
            botProbability.visibility = Button.GONE
            botWeb.visibility = Button.GONE
            Toast.makeText(
              applicationContext,
              "Your deposit is too large, please increase your netizens to a minimum: " +
                  (ValueFormat().decimalToDoge(balanceValue) - ValueFormat().decimalToDoge(balanceLimit)) * BigDecimal(32) / BigDecimal(100000),
              Toast.LENGTH_LONG
            ).show()
            loading.closeDialog()
          }
        } else {
          runOnUiThread {
            balance.text = "Balance : ${ValueFormat().decimalToDoge(balanceValue).toPlainString()} DOGE too low"
            botFibonacci.visibility = Button.GONE
            botProbability.visibility = Button.GONE
            botWeb.visibility = Button.GONE
            Toast.makeText(
              applicationContext,
              "has no remaining balance",
              Toast.LENGTH_SHORT
            ).show()
            loading.closeDialog()
          }
        }
      } else {
        runOnUiThread {
          balance.text = "DOGE : ERROR. click here to refresh"
          Toast.makeText(
            applicationContext,
            "your balance is not fully read. Please press the balance to refresh your balance",
            Toast.LENGTH_LONG
          ).show()
          botFibonacci.visibility = Button.GONE
          botProbability.visibility = Button.GONE
          botWeb.visibility = Button.GONE
          loading.closeDialog()
        }
      }
    }
  }
}
