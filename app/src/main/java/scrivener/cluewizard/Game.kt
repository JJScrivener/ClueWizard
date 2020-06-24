package scrivener.cluewizard

import android.content.res.Resources
import java.io.Serializable

class Game (num: Int, resources: Resources):Serializable {

    val numPlayers = num
    val categories = ArrayList<String>()
    val items = ArrayList<Array<String>>()
    val playerNames = ArrayList<String>()
    val playerStates = ArrayList<ArrayList<Int>>()
    private val questions = ArrayList<Question>()

    //player state variables
    private val no = 0
    private val yes = 1
    private val unsure = 2

    init{
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

        //This should be the only place that these resources are used directly
        items.add(resources.getStringArray(R.array.suspects))
        items.add(resources.getStringArray(R.array.weapons))
        items.add(resources.getStringArray(R.array.rooms))

        for(player in 0 until numPlayers){
            val temp = ArrayList<Int>()
            for(cat in items){
                for(item in cat){
                    if(player==0){
                        temp.add(no)
                    }else{
                        temp.add(unsure)
                    }
                }
            }
            playerStates.add(temp)
        }
    }

    fun addQuestion(asker: Int, answerer: Int, selectedItemIds: ArrayList<Int>, ans: Int){
        val newQuestion = Question(asker,answerer,selectedItemIds,ans)
        if(ans>=0) setYesRowState(newQuestion.answerer,newQuestion.ans)
        questions.add(0,newQuestion)
        checkQuestions()
    }

    private fun checkQuestions(){
        var newInfo = false

        //for all the players who couldn't answer the question set the state to no for all items in the question.
        var currentPlayer = (questions[0].asker+1)%numPlayers
        while((currentPlayer)!=questions[0].answerer && (currentPlayer)!=questions[0].asker){
            for(item in questions[0].items){
                playerStates[currentPlayer][item]=no
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
                    if(playerStates[question.answerer][item]==yes){
                        alreadyKnown=true
                        question.ans=item
                        break
                    }
                    //if we are unsure if the player has that item or not it is a possible answer
                    else if(playerStates[question.answerer][item]==unsure){
                        possibleAnswers.add(item)
                    }
                }
                //if we don't already know that the answerer has one of the items and the answerer has only one item that they could possibly have, that must be the answer.
                if(!alreadyKnown && possibleAnswers.size==1){
                    question.ans=possibleAnswers[0]
                    setYesRowState(question.answerer,question.ans)
                    newInfo=true
                }
            }
        }
        if(newInfo) checkQuestions()
    }

    private fun setYesRowState(yesPlayer: Int, item: Int){

        for(player in playerStates){
            player[item]=no
        }
        playerStates[yesPlayer][item]=yes
    }

    fun toggleMainPlayerRowState(item: Int){
        val mainState = when(playerStates[0][item]){
            1 -> 0
            else -> 1
        }
        val otherState = when(mainState){
            1 -> 0
            else -> 2
        }

        for(player in playerStates){
            player[item]=otherState
        }
        playerStates[0][item]=mainState
    }

    private data class Question (val asker: Int, val answerer: Int, val items: ArrayList<Int>, var ans:Int):Serializable
}