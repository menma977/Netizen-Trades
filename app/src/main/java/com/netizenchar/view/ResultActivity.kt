package com.netizenchar.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.netizenchar.MainActivity
import com.netizenchar.R
import com.netizenchar.config.Loading
import com.netizenchar.config.MD5
import com.netizenchar.controller.DataWebController
import com.netizenchar.model.SessionUser
import org.json.JSONObject
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
  private lateinit var sessionUser: SessionUser
  private lateinit var statusView: TextView
  private lateinit var description: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_result)

    loading = Loading(this)
    sessionUser = SessionUser(this)
    statusView = findViewById(R.id.statusTextView)
    description = findViewById(R.id.walletDescriptionTextView)

    statusView.text = intent.getSerializableExtra("status").toString()
    description.text =
      "We will return your capital and trading profit or the remaining cut loss from your capital to your doge wallet in a few moments.\n" +
          "This is your doge wallet: " + sessionUser.get("wallet")
    loading.closeDialog()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    val goTo = Intent(this, MainActivity::class.java)
    startActivity(goTo)
  }
}
