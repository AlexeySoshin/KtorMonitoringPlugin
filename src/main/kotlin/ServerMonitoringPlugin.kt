import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.*
import java.time.Instant

class ServerMonitoringPlugin(private val client: MockMetricsClient) {

    private val metricsKey: AttributeKey<Metrics> = AttributeKey("server metrics")

    companion object : ApplicationFeature<Application, Config, ServerMonitoringPlugin> {
        override val key: AttributeKey<ServerMonitoringPlugin> = AttributeKey("ServerMonitoringPlugin")

        override fun install(pipeline: Application, configure: Config.() -> Unit): ServerMonitoringPlugin {
            val configuration = Config().apply(configure)

            val feature = ServerMonitoringPlugin(configuration.client)
            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                feature.before(call)
                try {
                    proceed()
                    feature.success(call)
                }
                catch(e: Exception) {
                    feature.failure(call, e)
                }
                finally {
                    feature.after(call)
                }
            }
            return feature
        }
    }

    private fun after(call: ApplicationCall) {
        val before = call.attributes[metricsKey]
        client.time(call.request.uri, Instant.now().toEpochMilli() - before.toEpochMilli)
    }

    private fun before(call: ApplicationCall) {
        call.attributes.put(metricsKey, Metrics(Instant.now().toEpochMilli()))
    }

    data class Metrics(val toEpochMilli: Long)

    private fun failure(call: ApplicationCall, e: Exception) {
        client.incrementFailure(call.request.uri, e)
    }

    private fun success(call: ApplicationCall) {
        client.incrementSuccess(call.request.uri)
    }

    class Config {
        lateinit var client: MockMetricsClient
    }
}