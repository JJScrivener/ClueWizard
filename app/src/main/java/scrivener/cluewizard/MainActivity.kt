package scrivener.cluewizard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        }

        val mainBtnNew = findViewById<Button>(R.id.mainBtnNew)
        mainBtnNew?.setOnClickListener {startActivity<GameActivity>()}

        val mainBtnLoad = findViewById<Button>(R.id.mainBtnLoad)
        mainBtnLoad?.setOnClickListener {startActivity<LoadActivity>()}

        val mainBtnSettings = findViewById<Button>(R.id.mainBtnSettings)
        mainBtnSettings?.setOnClickListener {startActivity<SettingsActivity>()}

    }

    private inline fun <reified T: Activity> Activity.startActivity() {
        startActivity(Intent(this, T::class.java))
    }


}