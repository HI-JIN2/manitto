package party.manitto.domain.user

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.view.RedirectView
import party.manitto.domain.user.dto.AuthResponse
import party.manitto.domain.user.dto.GoogleAuthRequest
import party.manitto.domain.user.dto.KakaoAuthRequest
import party.manitto.global.entity.User
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val restTemplate: RestTemplate,
    @Value("\${google.client-id}") private val googleClientId: String,
    @Value("\${google.client-secret:}") private val googleClientSecret: String,
    @Value("\${app.base-url:}") private val appBaseUrl: String,
    @Value("\${app.frontend-base-url:http://localhost:3000}") private val frontendBaseUrl: String
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    // Google OAuth 리다이렉션 플로우: 1단계 - Google 로그인 페이지로 리다이렉트
    @GetMapping("/google")
    fun googleAuthRedirect(
        @RequestParam(value = "redirect_uri", required = false) frontendRedirectUri: String?
    ): RedirectView {
        try {
            if (googleClientId.isBlank()) {
                logger.error("Google login: GOOGLE_CLIENT_ID is not set")
                throw IllegalStateException("Google Client ID가 설정되지 않았습니다.")
            }

            // 프론트엔드 리다이렉트 URI (기본값: /auth/google/redirect)
            val redirectUri = frontendRedirectUri ?: "${getFrontendBaseUrl()}/auth/google/redirect"

            // 백엔드 콜백 URI (슬래시 중복 방지)
            val backendBase = getBackendBaseUrl().trimEnd('/')
            val backendCallbackUri = "$backendBase/api/auth/google/callback"

            // Google OAuth 인증 URL 생성
            val googleAuthUrl = buildString {
                append("https://accounts.google.com/o/oauth2/v2/auth?")
                append("client_id=${URLEncoder.encode(googleClientId, StandardCharsets.UTF_8)}")
                append(
                    "&redirect_uri=${
                        URLEncoder.encode(
                            backendCallbackUri,
                            StandardCharsets.UTF_8
                        )
                    }"
                )
                append("&response_type=code")
                append(
                    "&scope=${
                        URLEncoder.encode(
                            "openid email profile",
                            StandardCharsets.UTF_8
                        )
                    }"
                )
                append("&access_type=offline")
                append("&prompt=consent")
                append("&state=${URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)}")
            }

            logger.info("Google OAuth redirect: $googleAuthUrl")
            return RedirectView(googleAuthUrl)
        } catch (e: Exception) {
            logger.error("Google OAuth redirect error", e)
            throw e
        }
    }

    // Google OAuth 리다이렉션 플로우: 2단계 - Google에서 code를 받아서 access_token으로 교환 후 프론트엔드로 리다이렉트
    @GetMapping("/google/callback")
    fun googleAuthCallback(
        @RequestParam("code") code: String?,
        @RequestParam("state") state: String?,
        @RequestParam(value = "error", required = false) error: String?
    ): RedirectView {
        try {
            if (error != null) {
                logger.warn("Google OAuth error: $error")
                val frontendUrl = state ?: getFrontendBaseUrl()
                return RedirectView(
                    "$frontendUrl/auth?error=${
                        URLEncoder.encode(
                            error,
                            StandardCharsets.UTF_8
                        )
                    }"
                )
            }

            if (code.isNullOrBlank()) {
                logger.warn("Google OAuth: No code received")
                val frontendUrl = state ?: getFrontendBaseUrl()
                return RedirectView("$frontendUrl/auth?error=no_code")
            }

            if (googleClientId.isBlank() || googleClientSecret.isBlank()) {
                logger.error("Google login: GOOGLE_CLIENT_ID or GOOGLE_CLIENT_SECRET is not set")
                val frontendUrl = state ?: getFrontendBaseUrl()
                return RedirectView("$frontendUrl/auth?error=server_config")
            }

            val backendBase = getBackendBaseUrl().trimEnd('/')
            val backendCallbackUri = "$backendBase/api/auth/google/callback"

            // code를 access_token으로 교환
            val tokenResponse: GoogleTokenResponse = try {
                GoogleAuthorizationCodeTokenRequest(
                    NetHttpTransport(),
                    JacksonFactory(),
                    googleClientId,
                    googleClientSecret,
                    code,
                    backendCallbackUri
                ).execute()
            } catch (e: Exception) {
                logger.error("Failed to exchange code for token", e)
                val frontendUrl = state ?: getFrontendBaseUrl()
                return RedirectView("$frontendUrl/auth?error=token_exchange_failed")
            }

            val accessToken = tokenResponse.accessToken
            logger.info("Google OAuth: Successfully exchanged code for access token")

            // 프론트엔드로 리다이렉트 (access_token 포함)
            val frontendRedirectUri = state ?: "${getFrontendBaseUrl()}/auth/google/redirect"
            val redirectUrl = "$frontendRedirectUri?access_token=${
                URLEncoder.encode(
                    accessToken,
                    StandardCharsets.UTF_8
                )
            }"

            return RedirectView(redirectUrl)
        } catch (e: Exception) {
            logger.error("Google OAuth callback error", e)
            val frontendUrl = state ?: getFrontendBaseUrl()
            return RedirectView(
                "$frontendUrl/auth?error=${
                    URLEncoder.encode(
                        e.message ?: "unknown",
                        StandardCharsets.UTF_8
                    )
                }"
            )
        }
    }

    // Google OAuth 리다이렉션 플로우: 3단계 - access_token으로 사용자 정보 가져와서 JWT 발급
    @GetMapping("/google/verify")
    fun googleAuthVerify(
        @RequestParam("access_token") accessToken: String
    ): ResponseEntity<AuthResponse> {
        try {
            if (accessToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse(token = "", error = "Access token이 없습니다."))
            }

            // Google API로 사용자 정보 가져오기
            val userInfoResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                HttpEntity<String>(HttpHeaders().apply {
                    set("Authorization", "Bearer $accessToken")
                }),
                Map::class.java
            )

            val userInfo = userInfoResponse.body as? Map<*, *> ?: run {
                logger.warn("Google login: Failed to get user info")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse(token = "", error = "사용자 정보를 가져올 수 없습니다."))
            }

            val email = userInfo["email"] as? String
            val name = userInfo["name"] as? String
            val picture = userInfo["picture"] as? String

            if (email.isNullOrBlank()) {
                logger.warn("Google login: Email not found in user info")
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

    private fun getBackendBaseUrl(): String {
        return if (appBaseUrl.isNotBlank()) {
            appBaseUrl
        } else {
            "http://localhost:8080"
        }
    }

    private fun getFrontendBaseUrl(): String {
        return frontendBaseUrl
    }

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