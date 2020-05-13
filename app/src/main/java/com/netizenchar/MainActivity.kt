package com.netizenchar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.netizenchar.config.MD5
import com.netizenchar.controller.ValidateVersionController
import com.netizenchar.model.SessionUser
import com.netizenchar.view.HomeActivity
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
  private lateinit var goTo:Intent
  private lateinit var response: JSONObject
  private lateinit var sessionUser: SessionUser

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    sessionUser = SessionUser(this)
  }

  override fun onStart() {
    super.onStart()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["a"] = "VersiTrade"
      body["usertrade"] = sessionUser.get("username")
      body["passwordtrade"] = sessionUser.get("password")
      body["ref"] = MD5().convert(sessionUser.get("username") + sessionUser.get("password") + "versi" + "b0d0nk111179")
      response = ValidateVersionController(body).execute().get()
      runOnUiThread {
        if (response["code"] == 200) {
          if (response.getJSONObject("response")["Status"] == "0") {
            if (response.getJSONObject("response")["versiapk"] == BuildConfig.VERSION_NAME) {
              println(sessionUser.get("username"))
              if (sessionUser.get("username").isEmpty()) {
                goTo = Intent(applicationContext, LoginActivity::class.java)
                goTo.putExtra("lock", false)
                startActivity(goTo)
                finish()
              } else {
                sessionUser.set("wallet", response.getJSONObject("response")["walletdepo"].toString())
                goTo = Intent(applicationContext, HomeActivity::class.java)
                startActivity(goTo)
                finish()
              }
            } else {
              sessionUser.clear()
              goTo = Intent(applicationContext, LoginActivity::class.java)
              goTo.putExtra("lock", true)
              startActivity(goTo)
              finish()
            }
          } else {
            sessionUser.clear()
            goTo = Intent(applicationContext, LoginActivity::class.java)
            goTo.putExtra("lock", false)
            startActivity(goTo)
            finish()
          }
        } else {
          sessionUser.clear()
          goTo = Intent(applicationContext, LoginActivity::class.java)
          goTo.putExtra("lock", false)
          startActivity(goTo)
          finish()
        }
      }
    }
  }
}
