package party.manitto.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig {
    @Value("\${app.domain:}")
    private var domain: String = ""

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val patterns = buildList {
                    // localhost는 항상 허용
                    add("http://localhost:*")

                    // 환경변수에 도메인이 있으면 자동으로 패턴 생성
                    if (domain.isNotBlank()) {
                        domain.split(",").forEach { d ->
                            val trimmed = d.trim()
                            if (trimmed.isNotBlank()) {
                                // http, https, 포트 포함한 모든 조합 허용
                                add("http://$trimmed")
                                add("http://$trimmed:*")
                                add("https://$trimmed")
                                add("https://$trimmed:*")
                                // 서브도메인도 허용
                                add("http://*.$trimmed")
                                add("http://*.$trimmed:*")
                                add("https://*.$trimmed")
                                add("https://*.$trimmed:*")
                            }
                        }
                    } else {
                        // 도메인이 없으면 모든 Origin 허용 (개발/테스트용)
                        add("*")
                    }
                }.toTypedArray()

                registry.addMapping("/**")
                    .allowedOriginPatterns(*patterns)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
            }
        }
    }
}