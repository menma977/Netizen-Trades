package com.netizenchar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.netizenchar.model.SessionUser
import com.netizenchar.view.HomeActivity
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
  private lateinit var goTo:Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    Timer().schedule(100) {
      runOnUiThread {
        SessionUser(applicationContext).clear()
        if (SessionUser(applicationContext).get("username").isEmpty()) {
          goTo = Intent(applicationContext, LoginActivity::class.java)
          finish()
          startActivity(goTo)
        } else {
          goTo = Intent(applicationContext, HomeActivity::class.java)
          finish()
          startActivity(goTo)
        }
      }
    }
  }
}
