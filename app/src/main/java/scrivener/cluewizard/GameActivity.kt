package scrivener.cluewizard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import java.lang.Integer.max

//Todo: Change all hard coded strings to resources!
//Todo: Set up different languages (different countries have different names for the characters)
//Todo: Have a config activity to create custom profiles with names for all the items and colours etc.
//Todo: Hide the action bar when returning from a different activity or app
//Todo: Add a dialog to input how many cards each player has
class GameActivity : AppCompatActivity() {

    private val categories = ArrayList<String>()
    private val items = ArrayList<Array<String>>()
    private val playerNames = ArrayList<String>()
    private val playerBoxes = ArrayList<ArrayList<Box>>()
    private val questions = ArrayList<Question>()
    private var numPlayers = 0

    //States for the boxes
    private val no = 0
    private val yes = 1
    private val unsure = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        numPlayers = intent.getIntExtra("numPlayers",1)

        for(player in 0 until numPlayers){
            playerBoxes.add(ArrayList())
        }

        //set default player names
        for(player in 0..numPlayers){
            val newPlayer = when (player) {
                0 -> { "Me" }
                numPlayers -> { "No one" }
                else -> { "Player${player+1}" }
            }
            playerNames.add(newPlayer)
        }

        for(category in resources.getStringArray(R.array.categories)){
            categories.add(category)
        }

        //Todo: This should be the only place that I use these resources directly
        items.add(resources.getStringArray(R.array.suspects))
        items.add(resources.getStringArray(R.array.weapons))
        items.add(resources.getStringArray(R.array.rooms))

        buildGameLayout(numPlayers)

        if(numPlayers==1)   findViewById<Button>(R.id.game_btn).visibility=View.INVISIBLE
        else                buildQuestionDialog()

        inputPlayerNames()
    }

    private fun buildGameLayout(numPlayers : Int){

        val rows = ArrayList<View>()

        val gameLayout = findViewById<LinearLayout>(R.id.game_layout) //The layout where the game view will be displayed.
        var maxWidth=0 //the max width of the item labels. Used to make all the item TextViews the same size as the longest label.
        var itemIndex=0
        for((catIndex,category) in categories.withIndex()){ //For each category build the title row and then all the items rows for that category and add to the rows array list.
            val titleRow = LayoutInflater.from(this).inflate(R.layout.title_row,gameLayout,false)
            val title = titleRow.findViewById<TextView>(R.id.title)
            title.isClickable=true
            title.setOnClickListener { changeItemName(title) }
            title.text = category
            rows.add(titleRow)
            for(item in items[catIndex]){
                val itemRow = LayoutInflater.from(this).inflate(R.layout.item_row,gameLayout, false)

                val comment = itemRow.findViewById<TextView>(R.id.comment)
                comment.isSingleLine = true
                comment.setOnClickListener{addComment(comment)}

                val label = itemRow.findViewById<TextView>(R.id.label)
                label.text = item
                label.measure(0,0)
                label.isClickable=true
                label.setOnClickListener { changeItemName(label) }
                maxWidth=max(label.measuredWidth,maxWidth)

                val boxesLayout = itemRow.findViewById<LinearLayout>(R.id.boxes_layout)
                for(player in 0 until numPlayers){ //add a checkbox for each player

                    val image = ImageView(this)
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT)
                    val box = Box(image,itemIndex,player)
                    if(player==0){
                        image.isClickable=true
                        image.setOnClickListener{clickBox(box)}
                        box.state=State(no)
                    }
                    boxesLayout.addView(image,params)
                    playerBoxes[player].add(box)
                }

                rows.add(itemRow)
                itemIndex++
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

    private fun addComment(comment: TextView){

        val input = EditText(this)
        input.setText(comment.text)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Comment")
            .setView(input)
            .setPositiveButton("Set"
            ) { _, _ ->
                comment.text = input.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()

    }

    private fun changeItemName(item: TextView){
        val input = EditText(this)
        input.setText(item.text)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Change name")
            .setView(input)
            .setPositiveButton("Set"
            ) { _, _ ->
                item.text = input.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun buildQuestionDialog(){

        val gameBtn = findViewById<Button>(R.id.game_btn)

        val layout = LayoutInflater.from(this).inflate(R.layout.question,findViewById(R.id.activity_game),false)

        //Todo: Make the answer and asker spinners mutually exclusive. i.e. if ask = P1 then ans != P1 and vice versa.
        val playerAdapter: ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,playerNames)
        val askerSpin = layout.findViewById<Spinner>(R.id.question_asker_spin)
        askerSpin.adapter=playerAdapter
        val answererSpin = layout.findViewById<Spinner>(R.id.question_answerer_spin)
        answererSpin.adapter=playerAdapter

        val itemSpins = ArrayList<Spinner>()
        itemSpins.add(layout.findViewById(R.id.question_suspect_spin))
        itemSpins.add(layout.findViewById(R.id.question_weapon_spin))
        itemSpins.add(layout.findViewById(R.id.question_room_spin))

        for((index,spin) in itemSpins.withIndex()){
            val adapter: ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,items[index])
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
                for((index, spin) in itemSpins.withIndex()){
                    var id = spin.selectedItemId.toInt()
                    for(category in 0 until index){
                        id+=items[category].size
                    }
                    selectedItemIds.add(id)
                    selectedItems.add(spin.selectedItem.toString())
                }

                checkAnswer(asker, answerer, selectedItemIds, selectedItems)
            }
            .setNegativeButton("Cancel", null)
            .create()

        gameBtn.setOnClickListener {dialog.show()}

    }

    private fun checkAnswer(asker: Int, answerer: Int, selectedItemIds: ArrayList<Int>, selectedItems: ArrayList<String>) {
        if(asker==0 && answerer!=numPlayers){
            val input = Spinner(this)
            val inputAdapter: ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,selectedItems)
            input.adapter=inputAdapter
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("What was the answer?")
                .setView(input)
                .setPositiveButton("Set"
                ) { _, _ ->
                    val ans = selectedItemIds[input.selectedItemId.toInt()]
                    addQuestion(asker,answerer,selectedItemIds,ans)
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
        else{
            val ans = when(answerer){
                numPlayers -> -2
                else -> -1
            }
            addQuestion(asker,answerer,selectedItemIds,ans)
        }

    }

    private fun addQuestion(asker: Int, answerer: Int, selectedItemIds: ArrayList<Int>, ans: Int){
        val newQuestion = Question(asker,answerer,selectedItemIds,ans)
        if(ans>=0) updateRowState(playerBoxes[newQuestion.answerer][newQuestion.ans])
        questions.add(0,newQuestion)
        checkQuestions()
    }

    private fun inputPlayerNames(){
        for(index in (numPlayers-1) downTo  0){
            val input = EditText(this)
            input.setText(playerNames[index])
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("What is the player's name?")
                .setView(input)
                .setPositiveButton("Set"
                ) { _, _ ->
                    playerNames[index] = input.text.toString()
                }
                .setNegativeButton("Skip", null)
                .create()
            dialog.show()
        }
    }


    //Onclick listener for main player's boxes.
    //Toggles between yes and no and sets the same item for other players to: no if yes or unsure if no
    private fun clickBox(box: Box){
        box.state = box.state.not()
        val state = when(box.state.state){
            yes->State(no)
            else->State(unsure)
        }

        for(player in playerBoxes){
            if(player[box.item]!=box){
                player[box.item].state=state
            }
        }

    }

    private fun checkQuestions(){
        var newInfo = false

        //for all the players who couldn't answer the question set the state to no for all items in the question.
        var currentPlayer = (questions[0].asker+1)%numPlayers
        while((currentPlayer)!=questions[0].answerer && (currentPlayer)!=questions[0].asker){
            for(item in questions[0].items){
                playerBoxes[currentPlayer][item].state=State(no)
            }
            currentPlayer=(currentPlayer+1)%numPlayers
        }

        for(question in questions){
            //if we don't already know the answer to that question
            if(question.ans==-1){
                val possibleAnswers = ArrayList<Int>()
                var alreadyKnown = false
                for(item in question.items){
                    //if we know that the answerer has one of the items, we have to assume that it is the item that was shown.
                    if(playerBoxes[question.answerer][item].state.state==yes){
                        alreadyKnown=true
                        question.ans=item
                        break
                    }
                    //if we are unsure if the player has that item or not it is a possible answer
                    else if(playerBoxes[question.answerer][item].state.state==unsure){
                        possibleAnswers.add(item)
                    }
                }
                //if we don't already know that the answerer has one of the items and the answerer has only one item that they could possibly have, that must be the answer.
                if(!alreadyKnown && possibleAnswers.size==1){
                    question.ans=possibleAnswers[0]
                    updateRowState(playerBoxes[question.answerer][question.ans])
                    newInfo=true
                }
            }
        }
        if(newInfo) checkQuestions()
    }

    private fun updateRowState(box: Box){
        box.state=State(yes)
        for(player in playerBoxes){
            if(player[box.item]!=box){
                player[box.item].state=State(no)
            }
        }
    }

}