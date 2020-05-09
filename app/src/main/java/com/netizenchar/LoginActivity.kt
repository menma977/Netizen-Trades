package com.netizenchar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.netizenchar.config.Loading
import com.netizenchar.controller.LoginController
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
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    loading = Loading(this)
    sessionUser = SessionUser(this)
    email = findViewById(R.id.editTextEmail)
    password = findViewById(R.id.editTextPassword)
    login = findViewById(R.id.buttonLogin)

    loading.openDialog()

    login.setOnClickListener {
      val username = "PORWANTO529609"
      val password = "1234"
      loginDoge(username, password)
    }

    loading.closeDialog()
  }

  private fun loginDoge(username: String, password: String) {
    loading.openDialog()
    val body = HashMap<String, String>()
    body["a"] = "Login"
    body["key"] = "56f1816842b340a6bc07246801552702"
    body["username"] = username
    body["password"] = password
    body["Totp"] = "''"
    Timer().schedule(100) {
      response = LoginController.Web(body).execute().get()
      runOnUiThread {
        if (response["code"] == 200) {
          sessionUser.set("username", username)
          sessionUser.set("password", password)
          sessionUser.set("sessionCookie", response["response"].toString())
          goTo = Intent(applicationContext, HomeActivity::class.java)
          loading.closeDialog()
          finish()
          startActivity(goTo)
        } else {
          Toast.makeText(applicationContext, response["response"].toString(), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        }
      }
    }
  }
}
