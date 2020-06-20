package scrivener.cluewizard

class State (state: Int){
    private val no = 0
    private val yes = 1
    private val unsure = 2

    var state = when(state){
        yes -> yes
        no -> no
        else -> unsure
    }
    set(state){
        field = when(state){
            yes -> yes
            no -> no
            else -> unsure
        }
    }

    fun not():State{
        return when(this.state){
            yes-> (State(no))
            else-> (State(yes))
        }
    }

}