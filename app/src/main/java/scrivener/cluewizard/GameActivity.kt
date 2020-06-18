package scrivener.cluewizard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Integer.max


class GameActivity : AppCompatActivity() {

    private val playerBoxes = ArrayList<ArrayList<CheckBox>>()
    private val questions = ArrayList<Question>()
    private val categories = ArrayList<String>()
    private val resItems = ArrayList<Int>()
    private val items = ArrayList<Array<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val players = intent.getIntExtra("players",1)

        for(category in resources.getStringArray(R.array.categories)){
            categories.add(category)
        }
        resItems.add(R.array.suspects)
        resItems.add(R.array.weapons)
        resItems.add(R.array.rooms)

        for(item in resItems){
            items.add(resources.getStringArray(item))
        }

        buildGameLayout(players)
        buildQuestionDialog()
    }

    private fun buildGameLayout(players : Int){

        for(player in 0 until players){
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
                for(player in 0 until players){ //add a checkbox for each player
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

        //todo: create adaptors for players.
        //todo: think about how to show that a player couldn't answer a question.
        val askerSpin = layout.findViewById<Spinner>(R.id.question_asker_spin)
        val answererSpin = layout.findViewById<Spinner>(R.id.question_answerer_spin)

        val itemSpins = ArrayList<Spinner>()
        itemSpins.add(layout.findViewById(R.id.question_suspect_spin))
        itemSpins.add(layout.findViewById(R.id.question_weapon_spin))
        itemSpins.add(layout.findViewById(R.id.question_room_spin))

        for((index,spin) in itemSpins.withIndex()){
            ArrayAdapter.createFromResource(
                this,
                resItems[index],
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spin.adapter = adapter
            }
        }

        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Question")
            .setView(layout)
            .setPositiveButton("Set"
            ) { _, _ ->
                addQuestion()
            }
            .setNegativeButton("Cancel", null)
            .create()

        gameBtn.setOnClickListener {askQuestion(dialog)}

    }

    private fun askQuestion(dialog: AlertDialog){

        dialog.show()

    }

    private fun addQuestion(){
        //todo: pass in the actual values to make a new question (answer should be -1 for new question that main player didn't ask and answerer should be -1 if no body answered)
        val newQuestion = Question(1,1,1,1,1,1)
        questions.add(newQuestion)

        //todo: search the questions and make the magic happen
    }

}