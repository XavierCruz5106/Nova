package com.example.nova

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.nova.data.JellyfinRepository
import kotlinx.coroutines.launch

class LoginActivity : FragmentActivity() {

    private lateinit var serverUrlInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var jellyfinRepository: JellyfinRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        jellyfinRepository = JellyfinRepository(this)

        serverUrlInput = findViewById(R.id.server_url_input)
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.login_progress)

        // Set default server URL
        serverUrlInput.setText("http://10.0.2.2:8096")

        loginButton.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val serverUrl = serverUrlInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (serverUrl.isBlank() || username.isBlank() || password.isBlank()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled = false
        progressBar.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            val result = jellyfinRepository.login(serverUrl, username, password)

            result.onSuccess {
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()

                // Navigate to home
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }.onFailure { error ->
                progressBar.visibility = android.view.View.GONE
                loginButton.isEnabled = true
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_failed, error.message ?: "Unknown error"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}