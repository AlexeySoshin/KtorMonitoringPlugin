import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ServerTest {

    private fun testModule(mockClient: MockMetricsClient): Application.() -> Unit {
        return {
            install(ServerMonitoringPlugin) {
                this.client = MockMetricsClient()
            }
            routing {
                get("/*") {
                    call.respond(HttpStatusCode.OK, "status")
                }
            }
        }
    }

    @Test
    fun testRequest() {
        withTestApplication(testModule(MockMetricsClient())) {
            val response = handleRequest(HttpMethod.Get, "/hello").response

            assertEquals("status", response.content)
        }
    }
}