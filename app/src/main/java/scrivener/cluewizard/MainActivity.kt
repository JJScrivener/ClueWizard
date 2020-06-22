package scrivener.cluewizard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception
import java.lang.NumberFormatException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        }

        val mainBtnNew = findViewById<Button>(R.id.main_btn_new)
        mainBtnNew.setOnClickListener {startNewGame()}

    }

    private fun startNewGame(){
        val input = EditText(this)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("How many players?")
            .setView(input)
            .setPositiveButton("Go"
            ) { _, _ ->
                try{
                    val numPlayers = input.text.toString().toInt()
                    if(numPlayers in 1..6){
                        val intent = Intent(this,GameActivity::class.java)
                        intent.putExtra("numPlayers",numPlayers)
                        startActivity(intent)
                    }
                }catch(e: NumberFormatException){
                    println(e)
                }
                
                val text = "Please enter a valid number of players!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()

            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()



    }

}