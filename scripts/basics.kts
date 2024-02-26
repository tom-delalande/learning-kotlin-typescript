/*

Advantages (* = opinion)
    - Speed
    - Robust type system
    - Ergonomic syntax (*)
    - Better tooling (*)
    - Better support for functional programming
    - Better support for object-oriented programming
 */

// N/A
fun main() {
}

// console.log('Hello, world!')
println("Hello, world!")

// const name = 'Tom'
val name = "Tom"

// let age = 12
var age = 12

/*
function getEmptyString(): String {
    return ''
}
 */
fun getEmptyString(): String {
    return ""
}

/*
    interface Person {
        name: string
        age: number
    }
 */

data class Person(
    val name: String,
    val age: Int,
)

// let nullable: string | undefined = undefined
var nullable: String? = null

// type State = "WAITING" | "RUNNING" | "STOPPED"
enum class State {
    Waiting,
    Running,
    Stopped
}


// const arr = [1, 2, 3]
val arr = listOf(1, 2, 3)
val mutableArr = mutableListOf(1, 2, 3)

/*
const map = {
    "one": 1,
    "two": 2,
    "three": 3,
}
 */
val map = mapOf(
    "one" to 1,
    "two" to 2,
    "three" to 3
)

val mutableMap = mutableMapOf(
    "one" to 1,
    "two" to 2,
    "three" to 3
)
