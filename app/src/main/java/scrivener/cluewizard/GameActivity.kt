package scrivener.cluewizard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import java.lang.Integer.max


class GameActivity : AppCompatActivity() {

    private val playerBoxes = ArrayList<ArrayList<CheckBox>>()
    private val questions = ArrayList<Question>()
    private val categories = ArrayList<String>()
    private val items = ArrayList<Array<String>>()
    private val players = ArrayList<String>()
    private var numPlayers = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        numPlayers = intent.getIntExtra("numPlayers",1)

        for(player in 0..numPlayers){

            val newPlayer = when (player) {
                0 -> { "Me" }
                numPlayers -> { "No one" }
                else -> { "Player${player+1}" }
            }
            players.add(newPlayer)
        }

        for(category in resources.getStringArray(R.array.categories)){
            categories.add(category)
        }
        items.add(resources.getStringArray(R.array.suspects))
        items.add(resources.getStringArray(R.array.weapons))
        items.add(resources.getStringArray(R.array.rooms))

        buildGameLayout(numPlayers)
        buildQuestionDialog()
    }

    private fun buildGameLayout(numPlayers : Int){

        for(player in 0 until numPlayers){
            playerBoxes.add(ArrayList())
        }
        val rows = ArrayList<View>()

        val gameLayout = findViewById<LinearLayout>(R.id.game_layout) //The layout where the game view will be displayed.
        var maxWidth=0 //the max width of the item labels. Used to make all the item TextViews the same size as the longest label.

        for((index,category) in categories.withIndex()){ //For each category build and add the title row and then all the items rows for that category to a the rows array list.
            val titleRow = LayoutInflater.from(this).inflate(R.layout.title_row,gameLayout,false)
            val title = titleRow.findViewById<TextView>(R.id.title)
            title.text = category
            rows.add(titleRow)
            for(item in items[index]){
                val itemRow = LayoutInflater.from(this).inflate(R.layout.item_row,gameLayout, false)

                val comment = itemRow.findViewById<TextView>(R.id.comment)
                val commentText = "$item comment."
                comment.text=commentText
                comment.isSingleLine = true
                comment.setOnClickListener{addComment(comment)}

                val label = itemRow.findViewById<TextView>(R.id.label)
                label.text = item
                label.measure(0,0)
                maxWidth=max(label.measuredWidth,maxWidth)

                val boxesLayout = itemRow.findViewById<LinearLayout>(R.id.boxes_layout)
                for(player in 0 until numPlayers){ //add a checkbox for each player
                    val box = CheckBox(this)
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
                    val margin = (-5 * this.resources.displayMetrics.densityDpi / 160)
                    params.marginEnd=margin
                    if(player>0){
                        box.isClickable=false
                    }

                    boxesLayout.addView(box,params)
                    playerBoxes[player].add(box)
                }

                rows.add(itemRow)
            }
        }
        for(row in rows){ //add the rows to the layout
            val label = row.findViewById<TextView>(R.id.label)
            if(label!=null){
                label.width=maxWidth
            }
            gameLayout.addView(row)
        }

    }

    private fun addComment(textView:TextView){

        val input = EditText(this)
        input.setText(textView.text)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Comment")
            .setView(input)
            .setPositiveButton("Set"
            ) { _, _ ->
                textView.text = input.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()

    }

    private fun buildQuestionDialog(){

        val gameBtn = findViewById<Button>(R.id.game_btn)

        val layout = LayoutInflater.from(this).inflate(R.layout.question,findViewById(R.id.activity_game),false)

        //todo: think about how to show that a player couldn't answer a question.
        val playerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,players)
        val askerSpin = layout.findViewById<Spinner>(R.id.question_asker_spin)
        askerSpin.adapter=playerAdapter
        val answererSpin = layout.findViewById<Spinner>(R.id.question_answerer_spin)
        answererSpin.adapter=playerAdapter

        val itemSpins = ArrayList<Spinner>()
        itemSpins.add(layout.findViewById(R.id.question_suspect_spin))
        itemSpins.add(layout.findViewById(R.id.question_weapon_spin))
        itemSpins.add(layout.findViewById(R.id.question_room_spin))

        for((index,spin) in itemSpins.withIndex()){
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,items[index])
            spin.adapter=adapter
        }

        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Question")
            .setView(layout)
            .setPositiveButton("Set"
            ) { _, _ ->
                val asker = askerSpin.selectedItemId.toInt()
                val answerer = answererSpin.selectedItemId.toInt()

                val selectedItemIds = ArrayList<Int>()
                val selectedItems = ArrayList<String>()
                for(spin in itemSpins){
                    selectedItemIds.add(spin.selectedItemId.toInt())
                    selectedItems.add(spin.selectedItem.toString())
                }

                addQuestion(asker, answerer, selectedItemIds, selectedItems)
            }
            .setNegativeButton("Cancel", null)
            .create()

        gameBtn.setOnClickListener {askQuestion(dialog)}

    }

    private fun askQuestion(dialog: AlertDialog){

        dialog.show()

    }

    private fun addQuestion(asker: Int, answerer: Int, selectedItemIds: ArrayList<Int>, selectedItems: ArrayList<String>) {
        var ans = -1
        //Todo: Can you make it tidier by making the "No One" player have a value of -1 somehow?
        if(asker==0 && answerer!=numPlayers){
            //Todo: make a spinner to select the answer
            val input = Spinner(this)
            val inputAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,selectedItems)
            input.adapter=inputAdapter
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("What was the answer?")
                .setView(input)
                .setPositiveButton("Set"
                ) { _, _ ->
                    ans = input.selectedItemId.toInt()
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }

        val newQuestion = Question(asker,answerer,selectedItemIds[0],selectedItemIds[1],selectedItemIds[2],ans)
        questions.add(newQuestion)

        //todo: search the questions and make the magic happen
    }

}