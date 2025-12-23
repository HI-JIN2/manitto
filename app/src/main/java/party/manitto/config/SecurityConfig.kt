package party.manitto.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import party.manitto.global.JwtAuthFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors {}
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth/**").permitAll() // 로그인은 예외
                    // 게스트 모드 엔드포인트들
                    .requestMatchers("/api/parties/guest").permitAll() // 게스트 모드 파티 생성 (POST)
                    .requestMatchers("/api/parties/invite/*/guest/join")
                    .permitAll() // 게스트 모드 초대 코드 참가
                    .requestMatchers("/api/parties/*/guest/join").permitAll() // 게스트 모드 파티 참가
                    // 일반 조회 엔드포인트들
                    .requestMatchers(HttpMethod.GET, "/api/parties/**").permitAll() // 모든 GET 요청 허용
                    .requestMatchers("/api/parties/*/match").permitAll() // 매칭 실행 (POST)
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll() // Swagger UI 허용
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}