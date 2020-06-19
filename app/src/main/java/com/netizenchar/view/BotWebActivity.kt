package com.netizenchar.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.netizenchar.MainActivity
import com.netizenchar.R

class BotWebActivity : AppCompatActivity() {
  private lateinit var balance: TextView
  private lateinit var progressBar: ProgressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_web)

    balance = findViewById(R.id.textViewBalance)
    progressBar = findViewById(R.id.progressBar)

    Thread() {
      var time = System.currentTimeMillis()
      var i = 0
      while (i in 0..10) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 1000) {
          time = System.currentTimeMillis()
          if (i == 100) {
            progressBar.progress = 100
            break
          } else {
            progressBar.progress = i
          }
          i++
        }
      }
      runOnUiThread {
        progressBar.visibility = ProgressBar.GONE
        balance.text = intent.getSerializableExtra("profit").toString()
      }
    }.start()
  }

  override fun onBackPressed() {
    super.onBackPressed()
    val goTo = Intent(this, MainActivity::class.java)
    startActivity(goTo)
    finish()
  }
}
