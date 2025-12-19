package party.manitto.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Value("\${app.base-url:}")
    private lateinit var baseUrl: String

    @Bean
    fun customOpenAPI(): OpenAPI {
        val servers = mutableListOf(
            Server().url("http://localhost:8080").description("Local")
        )

        if (baseUrl.isNotBlank()) {
            servers.add(0, Server().url(baseUrl).description("Production"))
        }

        return OpenAPI()
            .info(
                Info()
                    .title("Manitto API")
                    .version("1.0.0")
                    .description("마니또 웹 서비스 API 문서")
            )
            .servers(servers)
    }
}