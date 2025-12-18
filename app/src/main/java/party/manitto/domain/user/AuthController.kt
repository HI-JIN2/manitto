package party.manitto.domain.user

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import party.manitto.global.entity.User

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    @Value("\${google.client-id}") private val googleClientId: String
) {
    @PostMapping("/google")
    fun googleLogin(@RequestBody body: Map<String, String>): ResponseEntity<Map<String, String>> {
        val credential = body["credential"] ?: return ResponseEntity.badRequest().build()

        // Google 토큰 검증
        val payload = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf(googleClientId)) // ✅ yml 값 사용
            .build()
            .verify(credential)
            ?.payload ?: return ResponseEntity.status(401).build()

        val email = payload["email"] as String

        // ✅ 유저 등록 or 조회
        val user = userRepository.findByEmail(email) ?: userRepository.save(User(email = email))

        // ✅ 우리 서버용 JWT 발급
        val token = jwtService.generateToken(user.email)

        return ResponseEntity.ok(mapOf("token" to token))
    }
}