class UserAccount {
    var uuid: String = ""
    var email: String = ""
    var balance: Double = 0.0
}

fun main() {
    val darel = UserAccount().apply {
        uuid = "erge22f"
        email = "darel@darel.darel"
        balance = 100000.0
    }
    
    println("email: ${darel.email}")
}
