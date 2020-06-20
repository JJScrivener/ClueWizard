package scrivener.cluewizard

import android.widget.ImageView
import java.lang.Exception

//The state can be 0 = no; 1 = yes; 2 = unsure
class Box (private val image: ImageView, val item: String, val player: Int) {

    private val no = 0
    private val yes = 1
    private val unsure = 2


    init{
        image.setImageResource(R.drawable.ic_unsurebox)
    }

    var state: State = State(2)
    set(value){
        when (value.state) {
            no -> { this.image.setImageResource(R.drawable.ic_nobox)}
            yes -> { this.image.setImageResource(R.drawable.ic_yesbox)}
            unsure -> { this.image.setImageResource(R.drawable.ic_unsurebox)}
            else -> throw Exception("Invalid box state")
        }
        field = value
    }

}