package party.manitto.domain.user

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import party.manitto.domain.user.dto.AuthResponse
import party.manitto.domain.user.dto.GoogleAuthRequest
import party.manitto.domain.user.dto.KakaoAuthRequest
import party.manitto.global.entity.User

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val restTemplate: RestTemplate,
    @Value("\${google.client-id}") private val googleClientId: String
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/google")
    fun googleLogin(@RequestBody req: GoogleAuthRequest): ResponseEntity<AuthResponse> {
        try {
            val credential = req.credential
            val redirectUri = req.redirectUri

            if (credential.isBlank()) {
                logger.warn("Google login: Empty credential")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse(token = "", error = "Google 인증 정보가 없습니다."))
            }

            if (googleClientId.isBlank()) {
                logger.error("Google login: GOOGLE_CLIENT_ID is not set")
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse(token = "", error = "서버 설정 오류가 발생했습니다."))
            }

            // redirect_uri 로깅 및 검증 (선택적)
            if (!redirectUri.isNullOrBlank()) {
                logger.debug("Google login: Redirect URI: $redirectUri")
                // 필요시 허용된 redirect URI 목록과 비교
                // val allowedRedirectUris = listOf("https://manitto-frontend.vercel.app", "http://localhost:3000", ...)
                // if (!allowedRedirectUris.contains(redirectUri)) {
                //     logger.warn("Google login: Invalid redirect URI: $redirectUri")
                // }
            }

            logger.debug("Google login: Verifying token with client ID: ${googleClientId.take(20)}...")

            // Google 토큰 검증
            val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
                .setAudience(listOf(googleClientId))
                .build()

            val idToken = verifier.verify(credential)
            if (idToken == null) {
                logger.warn("Google login: Token verification failed")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse(token = "", error = "Google 인증 토큰이 유효하지 않습니다."))
            }

            val payload = idToken.payload
            val email = payload["email"] as? String
            val name = payload["name"] as? String
            val picture = payload["picture"] as? String

            if (email.isNullOrBlank()) {
                logger.warn("Google login: Email not found in token")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse(token = "", error = "이메일 정보를 가져올 수 없습니다."))
            }

            logger.info("Google login: Success for email: $email")

            // ✅ 유저 등록 or 조회
            val user = userRepository.findByEmail(email)
                ?: userRepository.save(User(email = email, name = name, picture = picture))

            // ✅ 우리 서버용 JWT 발급
            val token = jwtService.generateToken(user.email)

            return ResponseEntity.ok(AuthResponse(token = token))
        } catch (e: Exception) {
            logger.error("Google login error", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse(token = "", error = "로그인 처리 중 오류가 발생했습니다: ${e.message}"))
        }
    }

    @PostMapping("/kakao")
    fun kakaoLogin(@RequestBody req: KakaoAuthRequest): ResponseEntity<AuthResponse> {
        val accessToken = req.accessToken

        // 카카오 사용자 정보 조회
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        val entity = HttpEntity<String>(headers)

        try {
            // 카카오 사용자 정보 조회 API 호출
            val response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                Map::class.java
            )

            val body = response.body as Map<*, *>
            val kakaoAccount = body["kakao_account"] as? Map<*, *>
            val properties = body["properties"] as? Map<*, *>

            val email = kakaoAccount?.get("email") as? String
                ?: return ResponseEntity.status(401).body(AuthResponse(token = ""))

            val name = (properties?.get("nickname") as? String) ?: (kakaoAccount["profile"] as? Map<*, *>)?.get("nickname") as? String
            val picture = (properties?.get("profile_image") as? String) ?: (kakaoAccount["profile"] as? Map<*, *>)?.get("profile_image_url") as? String

            // ✅ 유저 등록 or 조회
            val user = userRepository.findByEmail(email)
                ?: userRepository.save(User(email = email, name = name, picture = picture))

            // ✅ 우리 서버용 JWT 발급
            val token = jwtService.generateToken(user.email)

            return ResponseEntity.ok(AuthResponse(token = token))
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(401).build()
        }
    }
}