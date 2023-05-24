package com.example.piceditor.splash
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.piceditor.databinding.ActivitySplashBinding
import com.example.piceditor.MainActivity

class Splash : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    companion object {
        var isFromSplash: Boolean = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()

        },3000)
    }
}