package com.netizenchar.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.netizenchar.R

class HomeActivity : AppCompatActivity() {
  private lateinit var wallet: TextView
  private lateinit var copy: Button
  private lateinit var bot:Button

  private lateinit var clipboardManager: ClipboardManager
  private lateinit var clipData: ClipData
  private lateinit var goTo: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    wallet = findViewById(R.id.textViewWallet)
    copy = findViewById(R.id.buttonCopy)
    bot = findViewById(R.id.buttonBotMode)

    copy.setOnClickListener {
      clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipData = ClipData.newPlainText("Wallet", wallet.text.toString())
      clipboardManager.primaryClip = clipData
    }

    bot.setOnClickListener {
      goTo = Intent(this, BotActivity::class.java)
      startActivity(goTo)
    }
  }
}
