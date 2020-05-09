package com.netizenchar.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.netizenchar.R

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_result)
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }
}
