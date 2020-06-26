package scrivener.cluewizard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.lang.Integer.max


//Todo: Change all hard coded strings to resources!
//Todo: Set up different languages (different countries have different names for the characters)
//Todo: Have a config activity to create custom profiles with names for all the items and colours etc.
//Todo: Add a dialog to input how many cards each player has
class GameActivity : AppCompatActivity() {

    private val playerBoxes = ArrayList<ArrayList<IndexImage>>()
    private val comments = ArrayList<IndexTextView>()
    private val labels = ArrayList<IndexTextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)
        val game = intent.getSerializableExtra("game") as Game

        buildGameLayout(game)
        for((index, cat) in game.items.withIndex()){
            for((item) in cat.withIndex()){
                game.items[index][item]="Purple Pony"
            }
        }

        updateBoxImages(game)
        updateComments(game)
        updateLabels(game)

        if (game.numPlayers == 1) findViewById<Button>(R.id.game_btn).visibility = View.INVISIBLE
        else {buildQuestionDialog(game); inputPlayerNames(game)}

        save(game)
        val test = load()
        if(test != null){
            for(category in test.categories){
                println(category)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.apply {
                systemUiVisibility =
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
        }
    }

    private fun buildGameLayout(game: Game) {
        //add the check-box container arrays for each player
        for(player in 0 until game.numPlayers){
            playerBoxes.add(ArrayList())
        }
        //The views are inflated in a loop and then added to the rows array then after they have all been inflated they are added to main game layout
        val rows = ArrayList<View>()
        val gameLayout = findViewById<LinearLayout>(R.id.game_layout)

        //the max width of the item labels. Used to make all the item TextViews the same size as the longest label.
        var maxWidth = 0

        var itemIndex = 0
        //For each category in the game (Suspects, Weapons and Rooms).
        for ((catIndex, category) in game.categories.withIndex()) {
            val titleRow = LayoutInflater.from(this).inflate(R.layout.title_row, gameLayout, false)
            val title = titleRow.findViewById<TextView>(R.id.title)

            //Set onClickListener to change the text
            title.isSingleLine = true
            title.isClickable = true
            title.setOnClickListener { changeItemName(title) }
            title.text = category

            rows.add(titleRow)
            val indexTitle = IndexTextView(title, -1*(catIndex+1))
            labels.add(indexTitle)

            //For each item in the category
            for (item in game.items[catIndex]) {
                val itemRow = LayoutInflater.from(this).inflate(R.layout.item_row, gameLayout, false)

                val comment = itemRow.findViewById<TextView>(R.id.comment)
                comment.isSingleLine = true
                comment.isClickable = true
                comment.setOnClickListener { addComment(comment) }
                val indexComment = IndexTextView(comment,itemIndex)
                comments.add(indexComment)

                val label = itemRow.findViewById<TextView>(R.id.label)
                label.isSingleLine = true
                label.isClickable = true
                label.setOnClickListener { changeItemName(label) }
                label.text = item
                label.measure(0, 0)
                maxWidth = max(label.measuredWidth, maxWidth)
                val indexLabel = IndexTextView(label,itemIndex)
                labels.add(indexLabel)

                val boxesLayout = itemRow.findViewById<LinearLayout>(R.id.boxes_layout)
                for (player in 0 until game.numPlayers) { //add a checkbox for each player

                    val image = ImageView(this)
                    val box = IndexImage(image,itemIndex)
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    if (player == 0) {
                        image.isClickable = true
                        image.setOnClickListener { clickBox(game, box) }
                    }
                    boxesLayout.addView(image, params)
                    playerBoxes[player].add(box)
                }
                rows.add(itemRow)
                itemIndex++
            }
        }
        for (row in rows) { //add the rows to the layout
            val label = row.findViewById<TextView>(R.id.label)
            if (label != null) {
                label.width = maxWidth
            }
            gameLayout.addView(row)
        }
    }

    private fun addComment(comment: TextView) {
        val input = EditText(this)
        input.requestFocus()
        input.setText(comment.text)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Comment")
            .setView(input)
            .setPositiveButton(
                "Set"
            ) { _, _ ->
                comment.text = input.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private fun changeItemName(item: TextView) {
        val input = EditText(this)
        input.requestFocus()
        input.setText(item.text)
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Change name")
            .setView(input)
            .setPositiveButton(
                "Set"
            ) { _, _ ->
                item.text = input.text.toString()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private fun buildQuestionDialog(game: Game) {

        val gameBtn = findViewById<Button>(R.id.game_btn)

        val layout = LayoutInflater.from(this)
            .inflate(R.layout.question, findViewById(R.id.activity_game), false)

        val playerAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, game.playerNames)
        val askerSpin = layout.findViewById<Spinner>(R.id.question_asker_spin)
        askerSpin.adapter = playerAdapter
        val answererSpin = layout.findViewById<Spinner>(R.id.question_answerer_spin)
        answererSpin.adapter = playerAdapter

        val itemSpins = ArrayList<Spinner>()
        itemSpins.add(layout.findViewById(R.id.question_suspect_spin))
        itemSpins.add(layout.findViewById(R.id.question_weapon_spin))
        itemSpins.add(layout.findViewById(R.id.question_room_spin))

        for ((index, spin) in itemSpins.withIndex()) {
            val adapter: ArrayAdapter<String> =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, game.items[index])
            spin.adapter = adapter
        }

        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Question")
            .setView(layout)
            .setPositiveButton(
                "Set"
            ) { _, _ ->
                val asker = askerSpin.selectedItemId.toInt()
                val answerer = answererSpin.selectedItemId.toInt()
                val selectedItemIds = ArrayList<Int>()
                val selectedItems = ArrayList<String>()
                for ((index, spin) in itemSpins.withIndex()) {
                    var id = spin.selectedItemId.toInt()
                    for (category in 0 until index) {
                        id += game.items[category].size
                    }
                    selectedItemIds.add(id)
                    selectedItems.add(spin.selectedItem.toString())
                }
                checkAnswer(game, asker, answerer, selectedItemIds, selectedItems)
            }
            .setNegativeButton("Cancel", null)
            .create()
        gameBtn.setOnClickListener { dialog.show() }
    }

    private fun checkAnswer(game: Game, asker: Int, answerer: Int, selectedItemIds: ArrayList<Int>, selectedItems: ArrayList<String>) {
        if (asker == 0 && answerer != game.numPlayers) {
            val input = Spinner(this)
            val inputAdapter: ArrayAdapter<String> =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, selectedItems)
            input.adapter = inputAdapter
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("What was the answer?")
                .setView(input)
                .setPositiveButton(
                    "Set"
                ) { _, _ ->
                    val ans = selectedItemIds[input.selectedItemId.toInt()]
                    game.addQuestion(asker, answerer, selectedItemIds, ans)
                    updateBoxImages(game)
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        } else {
            val ans = when (answerer) {
                game.numPlayers -> -2
                else -> -1
            }
            game.addQuestion(asker, answerer, selectedItemIds, ans)
            updateBoxImages(game)
        }
    }

    private fun inputPlayerNames(game: Game) {
        for (index in (game.numPlayers - 1) downTo 0) {
            val input = EditText(this)
            input.requestFocus()
            input.hint = game.playerNames[index]
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("What is the player's name?")
                .setView(input)
                .setPositiveButton(
                    "Set"
                ) { _, _ ->
                    game.playerNames[index] = input.text.toString()
                }
                .setNegativeButton("Skip", null)
                .create()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()
        }
    }

    private fun clickBox(game: Game, indexImage: IndexImage){
        game.toggleMainPlayerRowState(indexImage.index)
        for((playerIndex, player) in playerBoxes.withIndex()){
            setBoxImage(game, player, playerIndex, indexImage.index)
        }
    }

    private fun updateBoxImages(game: Game){
        //For each player in the game
        for((playerIndex,player) in playerBoxes.withIndex()){
            //For each item of that player
            for((itemIndex) in player.withIndex()){
                //Set the image based on the state of that item
                setBoxImage(game, player, playerIndex, itemIndex)
            }
        }
    }

    private fun updateLabels(game: Game){
        //Todo: This seems pretty dirty, think of a neater way to do this
        //Todo: Do I really need the indexed item class? Isn't an ArrayList exactly the same?
        //Todo: I would need a categories ArrayList as well as a labels one
        val flatItems = ArrayList<String>()
        for(cat in game.items){
            for(item in cat){
                flatItems.add(item)
            }
        }

        for(label in labels){
            //if a category label
            if(label.index<0){
                label.text.text = game.categories[-1*(label.index+1)]
            }
            //else it's an item label
            else{
                label.text.text = flatItems[label.index]
            }
        }
    }

    private fun updateComments(game: Game){
        for(comment in comments){
            comment.text.text = game.playerComments[comment.index]
        }
    }

    private fun setBoxImage(game: Game, player: ArrayList<IndexImage>, playerIndex: Int, itemIndex: Int){
        when(game.playerStates[playerIndex][itemIndex]){
            0 -> player[itemIndex].image.setImageResource(R.drawable.ic_nobox)
            1 -> player[itemIndex].image.setImageResource(R.drawable.ic_yesbox)
            else -> player[itemIndex].image.setImageResource(R.drawable.ic_unsurebox)
        }
    }

    private fun save(game: Game) {
        try {
            val fileOut = FileOutputStream(filesDir.absolutePath+"/test.ser")
            val out = ObjectOutputStream(fileOut)
            out.writeObject(game)
            out.close()
            fileOut.close()
        } catch (i: IOException) {
            i.printStackTrace()
        }
    }

    private fun load():Game? {
        try {
            val fileIn = FileInputStream(filesDir.absolutePath+"/test.ser")
            val `in` = ObjectInputStream(fileIn)
            val ret = `in`.readObject() as Game
            `in`.close()
            fileIn.close()
            return(ret)
        } catch (i: IOException) {
            i.printStackTrace()
        } catch (c: ClassNotFoundException) {
            c.printStackTrace()
        }
        return(null)
    }

}