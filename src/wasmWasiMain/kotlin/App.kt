import kotlinx.io.buffered
import kotlinx.io.readLine

fun main() {
    println("Started. Enter something and press enter")
    WasiStdInputSource().buffered().use { source ->
        while (true) {
            source.readLine()?.let { line -> println("Wasm received: $line") } ?: break
        }
    }
    println("Done. Bye")
}