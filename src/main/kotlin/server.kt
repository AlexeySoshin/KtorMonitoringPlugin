import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import java.util.*
import kotlin.random.Random

fun main() {
    val port = 9000
    val metricsClient = MockMetricsClient()
    embeddedServer(Netty, port = port) {
        routing {
            // We'll return the same response for any URL
            get("/*") {
                // With some minor random delay
                delay(Random.nextLong(10))

                // Fail some requests randomly
                if (Random.nextInt(10) == 0) {
                    throw RuntimeException("Something bad happened")
                }
                call.respondText("Hello! ${Calendar.getInstance().time}")
            }
        }
    }.start()

    println("open http://localhost:$port/")
}


class MockMetricsClient {
    fun incrementSuccess(url: String) =
        println("${Calendar.getInstance().time} Success: $url")

    fun incrementFailure(url: String, e: Throwable) =
        println("${Calendar.getInstance().time} Failure: $url, $e")

    fun time(url: String, delta: Long) =
        println("${Calendar.getInstance().time} $url took ${delta}ms")
}