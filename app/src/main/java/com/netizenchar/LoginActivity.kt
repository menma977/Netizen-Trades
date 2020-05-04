package com.netizenchar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.netizenchar.view.HomeActivity

class LoginActivity : AppCompatActivity() {
  private lateinit var goTo: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    goTo = Intent(this, HomeActivity::class.java)
    startActivity(goTo)
    finish()
  }
}
