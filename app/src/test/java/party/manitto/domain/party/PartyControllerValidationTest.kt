package party.manitto.domain.party

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import party.manitto.config.SecurityConfig
import party.manitto.domain.party.dto.CreatePartyRequest
import party.manitto.domain.user.JwtService
import party.manitto.global.GlobalExceptionHandler

@WebMvcTest(
    controllers = [PartyController::class],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [SecurityConfig::class])]
)
class PartyControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    lateinit var partyService: PartyService
    
    // SecurityConfig를 제외했으므로 JwtService 등의 bean 의존성 문제 해결을 위해 Mock 필요할 수 있음
    // WebMvcTest는 SecurityConfig를 로드하려고 시도할 수 있으므로, 
    // 실제로는 excludeFilters로 SecurityConfig 제외하고, WithMockUser로 우회하는 전략 사용
    @MockkBean
    lateinit var jwtService: JwtService

    @MockkBean
    lateinit var userRepository: party.manitto.domain.user.UserRepository

    @Test
    @WithMockUser
    fun `createParty - 이름이 비어있으면 400 응답`() {
        val request = CreatePartyRequest(name = "") // Invalid

        mockMvc.post("/api/parties") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { value("BAD_REQUEST") }
        }
    }
}
