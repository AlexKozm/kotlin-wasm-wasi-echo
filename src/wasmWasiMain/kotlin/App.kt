fun main() {
    try {
        val input = readln()
        println("Echo: $input")
    } catch (e: Throwable) {
        println("Oh, can't read using kotlin io: ${e.message}")
    }
    println("Hello from Kotlin via WASI")
}