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
    private var baseUrl: String = ""

    @Bean
    fun customOpenAPI(): OpenAPI {
        val servers = mutableListOf<Server>()

        // 프로덕션 URL이 있으면 우선 사용
        if (baseUrl.isNotBlank()) {
            servers.add(Server().url(baseUrl).description("Production"))
        }

        // 로컬 개발용
        servers.add(Server().url("http://localhost:8080").description("Local"))

        // 동적 서버 (현재 요청의 호스트 사용)
        servers.add(Server().url("/").description("Current Server"))

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