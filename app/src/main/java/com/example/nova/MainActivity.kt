package com.example.nova

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.nova.data.JellyfinRepository

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val jellyfinRepository = JellyfinRepository(this)
        if (!jellyfinRepository.isLoggedIn()) {
            // Redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }
    }
}