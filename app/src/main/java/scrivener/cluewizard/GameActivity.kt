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
//Todo: Hide the action bar when returning from a different activity or app
//Todo: Add a dialog to input how many cards each player has
class GameActivity : AppCompatActivity() {

    private val playerBoxes = ArrayList<ArrayList<Box>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)
        val game = intent.getSerializableExtra("game") as Game

        buildGameLayout(game)

        if (game.numPlayers == 1) findViewById<Button>(R.id.game_btn).visibility = View.INVISIBLE
        else buildQuestionDialog(game)

        inputPlayerNames(game)
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
        for(player in 0 until game.numPlayers){
            playerBoxes.add(ArrayList())
        }
        val rows = ArrayList<View>()
        val gameLayout =
            findViewById<LinearLayout>(R.id.game_layout) //The layout where the game view will be displayed.
        var maxWidth =
            0 //the max width of the item labels. Used to make all the item TextViews the same size as the longest label.
        var itemIndex = 0
        for ((catIndex, category) in game.categories.withIndex()) { //For each category build the title row and then all the items rows for that category and add to the rows array list.
            val titleRow = LayoutInflater.from(this).inflate(R.layout.title_row, gameLayout, false)
            val title = titleRow.findViewById<TextView>(R.id.title)
            title.isClickable = true
            title.setOnClickListener { changeItemName(title) }
            title.text = category
            rows.add(titleRow)
            for (item in game.items[catIndex]) {
                val itemRow =
                    LayoutInflater.from(this).inflate(R.layout.item_row, gameLayout, false)

                val comment = itemRow.findViewById<TextView>(R.id.comment)
                comment.isSingleLine = true
                comment.setOnClickListener { addComment(comment) }

                val label = itemRow.findViewById<TextView>(R.id.label)
                label.text = item
                label.measure(0, 0)
                label.isClickable = true
                label.setOnClickListener { changeItemName(label) }
                maxWidth = max(label.measuredWidth, maxWidth)

                val boxesLayout = itemRow.findViewById<LinearLayout>(R.id.boxes_layout)
                for (player in 0 until game.numPlayers) { //add a checkbox for each player

                    val image = ImageView(this)
                    val box = Box(image,itemIndex)
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
            updateBoxImages(game)
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

    private fun clickBox(game: Game, box: Box){
        game.toggleMainPlayerRowState(box.itemIndex)
        for((playerIndex, player) in playerBoxes.withIndex()){
            when(game.playerStates[playerIndex][box.itemIndex]){
                0 -> player[box.itemIndex].imageView.setImageResource(R.drawable.ic_nobox)
                1 -> player[box.itemIndex].imageView.setImageResource(R.drawable.ic_yesbox)
                else -> player[box.itemIndex].imageView.setImageResource(R.drawable.ic_unsurebox)
            }
        }
    }

    private fun updateBoxImages(game: Game){
        for((playerIndex,player) in playerBoxes.withIndex()){
            for((itemIndex) in player.withIndex()){
                when(game.playerStates[playerIndex][itemIndex]){
                    0 -> player[itemIndex].imageView.setImageResource(R.drawable.ic_nobox)
                    1 -> player[itemIndex].imageView.setImageResource(R.drawable.ic_yesbox)
                    else -> player[itemIndex].imageView.setImageResource(R.drawable.ic_unsurebox)
                }
            }
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

    private data class Box(val imageView: ImageView, val itemIndex: Int)

}