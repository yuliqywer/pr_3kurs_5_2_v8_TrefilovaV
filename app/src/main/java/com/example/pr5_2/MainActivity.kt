package com.example.pr5_2

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("username", "")

        if (savedName.isNullOrEmpty()) {
            replaceFragment(reg_fragment())
        } else {
            replaceFragment(api_fragment())
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}