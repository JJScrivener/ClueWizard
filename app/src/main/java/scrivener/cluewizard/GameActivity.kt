package scrivener.cluewizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        val players = 4
        val rows = ArrayList<View>()
        val categories = resources.getStringArray(R.array.categories)
        val items = ArrayList<Array<String>>()
        items.add(resources.getStringArray(R.array.suspects))
        items.add(resources.getStringArray(R.array.weapons))
        items.add(resources.getStringArray(R.array.rooms))


        val parentLayout = findViewById<LinearLayout>(R.id.game_linear_layout)


        for((index,cat) in categories.withIndex()){
            val titleRow = LayoutInflater.from(this).inflate(R.layout.title_row,parentLayout,false)
            val title = titleRow.findViewById<TextView>(R.id.title)
            title.text = cat
            rows.add(titleRow)
            for(name in items[index]){
                val itemRow = LayoutInflater.from(this).inflate(R.layout.item_row,parentLayout, false)
                val item = itemRow.findViewById<TextView>(R.id.label)
                item.text = name
                rows.add(itemRow)
            }
        }
        for(row in rows){
            parentLayout.addView(row)
        }

    }


}