fun sumaTotal(montos: List<Double?>): Double {
    return montos.sumOf { monto -> monto ?: 0.0 }
}

fun main() {
    val montos = listOf(100.0, null, 22.0, null, 1.0)
	val total = sumaTotal(montos)
    
    println(total)
}
