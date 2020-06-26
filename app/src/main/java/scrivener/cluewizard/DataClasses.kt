package scrivener.cluewizard

import android.widget.ImageView
import android.widget.TextView
import java.io.Serializable

data class IndexImage(val image: ImageView, val index: Int)
data class IndexTextView(val text: TextView, val index: Int)
data class Question (val asker: Int, val answerer: Int, val items: ArrayList<Int>, var ans:Int): Serializable