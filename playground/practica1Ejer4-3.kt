data class Transaccion(val categoria: String, val monto: Double)

fun main() {
    val numeros = (1..20).toList()
    val cuadradosMultiplosDeTres = numeros
        .filter {it % 3 == 0}
        .map {it * it}
        
    println(cuadradosMultiplosDeTres)
    
    val movimientos = listOf(
    	Transaccion("Comida", 150.0),
        Transaccion("Transporte", 50.0),
        Transaccion("Comida", 300.0),
        Transaccion("Ocio", 200.0),
        Transaccion("Transporte", 120.0),
        Transaccion("Ocio", 80.0)
    )
    
    val reportarGastos = movimientos
    	.filter {it.monto > 100.0}
        .groupBy {it.categoria}
        .mapValues {(_, transacciones) -> 
            transacciones.map {it.monto}.average()
        }
        
    println(reportarGastos)
}
