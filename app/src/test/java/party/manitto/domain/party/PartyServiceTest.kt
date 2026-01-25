package party.manitto.domain.party

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.domain.participant.ParticipantRepository
import party.manitto.global.CustomException
import party.manitto.global.ErrorCode
import party.manitto.global.entity.Party
import party.manitto.global.entity.User
import java.util.*

@ExtendWith(MockKExtension::class)
class PartyServiceTest {

    @MockK
    lateinit var partyRepository: PartyRepository

    @MockK
    lateinit var matchedResultRepository: MatchedResultRepository

    @MockK
    lateinit var participantRepository: ParticipantRepository

    @InjectMockKs
    lateinit var partyService: PartyService

    @Test
    fun `createParty - 성공적으로 파티를 생성한다`() {
        // given
        val user = User(id = 1L, email = "test@test.com", name = "tester")
        val party = Party(id = 1L, name = "My Party", inviteCode = "ABCDEF", host = user)

        every { partyRepository.existsByInviteCode(any()) } returns false
        every { partyRepository.save(any()) } returns party

        // when
        val response = partyService.createParty("My Party", user)

        // then
        assertNotNull(response)
        assertEquals("My Party", response.name)
        verify { partyRepository.save(any()) }
    }

    @Test
    fun `getPartyById - 존재하지 않는 파티 조회 시 예외 발생`() {
        // given
        every { partyRepository.findById(999L) } returns Optional.empty()

        // when & then
        val exception = assertThrows<CustomException> {
            partyService.getPartyById(999L)
        }
        assertEquals(ErrorCode.PARTY_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `getPartyByInviteCode - 유효하지 않은 코드로 조회 시 예외 발생`() {
        // given
        every { partyRepository.findByInviteCode("INVALID") } returns null

        // when & then
        val exception = assertThrows<CustomException> {
            partyService.getPartyByInviteCode("INVALID")
        }
        assertEquals(ErrorCode.INVALID_INVITE_CODE, exception.errorCode)
    }
}
