package party.manitto.domain.user

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
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
    @PostMapping("/google")
    fun googleLogin(@RequestBody req: GoogleAuthRequest): ResponseEntity<AuthResponse> {
        val credential = req.credential

        // Google 토큰 검증
        val payload = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf(googleClientId)) // ✅ yml 값 사용
            .build()
            .verify(credential)
            ?.payload ?: return ResponseEntity.status(401).build()

        val email = payload["email"] as String
        val name = payload["name"] as? String
        val picture = payload["picture"] as? String

        // ✅ 유저 등록 or 조회
        val user = userRepository.findByEmail(email) 
            ?: userRepository.save(User(email = email, name = name, picture = picture))

        // ✅ 우리 서버용 JWT 발급
        val token = jwtService.generateToken(user.email)

        return ResponseEntity.ok(AuthResponse(token = token))
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