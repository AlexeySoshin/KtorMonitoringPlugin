import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.*
import java.time.Instant

class ServerMonitoringPlugin(private val client: MockMetricsClient) {

    private val metricsKey: AttributeKey<Metrics> = AttributeKey("Metrics from Server Monitoring")

    companion object : ApplicationFeature<Application, Config, ServerMonitoringPlugin> {
        override val key: AttributeKey<ServerMonitoringPlugin> = AttributeKey("just for debugging")

        override fun install(pipeline: Application, configure: Config.() -> Unit): ServerMonitoringPlugin {
            val config = Config().apply(configure)

            val plugin = ServerMonitoringPlugin(config.client)

            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                plugin.before(call)
                try {
                    proceed()
                    plugin.success(call)
                }
                catch (e: Exception) {
                    plugin.failure(call, e)
                }
                finally {
                    plugin.after(call)
                }
            }

            return plugin
        }

    }

    private fun after(call: ApplicationCall) {
        val before = call.attributes[metricsKey]

        client.time(call.request.uri, Instant.now().toEpochMilli() - before.toEpochMilli)
    }

    private fun before(call: ApplicationCall) {
        call.attributes.put(metricsKey, Metrics(Instant.now().toEpochMilli()))
    }

    private fun failure(call: ApplicationCall, e: Exception) {
        client.incrementFailure(call.request.uri, e)
    }

    private fun success(call: ApplicationCall) {
        client.incrementSuccess(call.request.uri)
    }

    class Config {
        lateinit var client: MockMetricsClient
    }

    data class Metrics(val toEpochMilli: Long)
}