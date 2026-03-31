data class UserAccount(var uuid: String, var email: String, var balance: Double)

fun main() {
    // construccion, copy y toString
    val nacho = UserAccount("12312", "nacho@nacho.nacho", 0.0)
    val nachoCopia = nacho.copy(balance = 5000.0)
    println("Copia: $nachoCopia")
    
    // getter y toString
    println(nacho.uuid)
    println("Antes: $nacho")
    
    // setter
    nacho.email = "darel@darel.darel"
    println("Despues: $nacho")
    
    // equals y ==
    println(nachoCopia.uuid.equals(nacho.uuid))
    println(nachoCopia.uuid == nacho.uuid)
    println(nachoCopia == nacho)
    
    // hashCode
    println("${nachoCopia.hashCode()} ${nacho.hashCode()}")
    println(nachoCopia.hashCode().equals(nacho.hashCode()))
}
