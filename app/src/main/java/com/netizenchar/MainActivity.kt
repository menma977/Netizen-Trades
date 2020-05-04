package com.netizenchar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
  private lateinit var goTo:Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    goTo = Intent(this, LoginActivity::class.java)
    startActivity(goTo)
    finish()
  }
}
