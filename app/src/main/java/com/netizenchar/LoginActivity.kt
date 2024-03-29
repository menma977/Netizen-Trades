package com.netizenchar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.netizenchar.config.Loading
import com.netizenchar.config.MD5
import com.netizenchar.controller.DogeController
import com.netizenchar.controller.WebController
import com.netizenchar.model.SessionUser
import com.netizenchar.view.HomeActivity
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class LoginActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var loading: Loading
  private lateinit var sessionUser: SessionUser
  private lateinit var response: JSONObject

  private lateinit var email: EditText
  private lateinit var password: EditText
  private lateinit var login: Button
  private lateinit var version: TextView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    val lock = intent.getSerializableExtra("lock").toString().toBoolean()
    val versionResponse = intent.getSerializableExtra("version").toString()
    loading = Loading(this)
    sessionUser = SessionUser(this)
    email = findViewById(R.id.editTextEmail)
    password = findViewById(R.id.editTextPassword)
    login = findViewById(R.id.buttonLogin)
    version = findViewById(R.id.versionTextView)

    doRequestPermission()

    loading.openDialog()

    version.text = versionResponse

    if (lock) {
      email.isEnabled = false
      password.isEnabled = false
      login.visibility = Button.GONE
    }

    login.setOnClickListener {
      when {
        email.text.isEmpty() -> {
          Toast.makeText(this, "Your email cannot be empty", Toast.LENGTH_SHORT).show()
        }
        password.text.isEmpty() -> {
          Toast.makeText(this, "Your password cannot be empty", Toast.LENGTH_SHORT).show()
        }
        else -> {
          login(email.text.toString(), password.text.toString())
        }
      }
    }

    loading.closeDialog()
  }

  private fun login(email: String, password: String) {
    loading.openDialog()
    val body = HashMap<String, String>()
    body["a"] = "LoginSession"
    body["username"] = email
    body["password"] = password
    body["ref"] = MD5().convert(email + password + "b0d0nk111179")
    Timer().schedule(100) {
      response = WebController(body).execute().get()
      runOnUiThread {
        if (response["code"] == 200) {
          sessionUser.set("usernameWeb", email)
          sessionUser.set("wallet", response.getJSONObject("data")["walletdepo"].toString())
          sessionUser.set("limitDeposit", response.getJSONObject("data")["maxdepo"].toString())
          val usernameDoge = response.getJSONObject("data")["userdoge"].toString()
          val passwordDoge = response.getJSONObject("data")["passdoge"].toString()
          loginDoge(usernameDoge, passwordDoge)
        } else {
          Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun loginDoge(username: String, password: String) {
    val body = HashMap<String, String>()
    body["a"] = "Login"
    body["key"] = "56f1816842b340a6bc07246801552702"
    body["username"] = username
    body["password"] = password
    body["Totp"] = "''"
    Timer().schedule(100) {
      response = DogeController(body).execute().get()
      runOnUiThread {
        if (response["code"] == 200) {
          sessionUser.set("username", username)
          sessionUser.set("password", password)
          sessionUser.set("sessionCookie", response.getJSONObject("data")["SessionCookie"].toString())
          goTo = Intent(applicationContext, HomeActivity::class.java)
          startActivity(goTo)
          loading.closeDialog()
          finish()
        } else {
          Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun doRequestPermission() {
    if (
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) != PackageManager.PERMISSION_GRANTED
      || ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
      ) != PackageManager.PERMISSION_GRANTED
      || ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WAKE_LOCK
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(
          arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK
          ), 100
        )
      }
    }
  }
}
