fun Int.esPar(): Boolean {
    return this % 2 == 0
}

fun Int.esPrimo(): Boolean {
    if (this < 2) return false
    
    for (i in 2 until this) {
        if (this % i == 0) return false
    }
    
    return true
}

fun main() {
    val numero = 7
    println(numero.esPar())
    println(numero.esPrimo())
}