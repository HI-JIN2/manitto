package party.manitto.domain.match

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import party.manitto.domain.participant.ParticipantRepository
import party.manitto.global.CustomException
import party.manitto.global.ErrorCode
import party.manitto.global.entity.Participant
import party.manitto.global.entity.Party
import party.manitto.global.entity.User

@ExtendWith(MockKExtension::class)
class MatchServiceTest {

    @MockK
    lateinit var participantRepository: ParticipantRepository

    @MockK
    lateinit var matchedResultRepository: MatchedResultRepository

    @MockK
    lateinit var emailJobRepository: EmailJobRepository

    @InjectMockKs
    lateinit var matchService: MatchService

    @Test
    fun `matchAndSave - 참가자가 2명 미만이면 예외 발생`() {
        // given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party", inviteCode = "CODE", host = null)
        val participant1 = Participant(id = 1L, party = party, user = null, guestName = "A", guestEmail = "a@a.com")
        
        every { participantRepository.findByPartyId(partyId) } returns listOf(participant1)

        // when & then
        val exception = assertThrows<CustomException> {
            matchService.matchAndSave(partyId)
        }
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.errorCode)
    }

    @Test
    fun `matchAndSave - 정상적으로 매칭하고 이메일을 저장한다`() {
        // given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party", inviteCode = "CODE", host = null)
        val p1 = Participant(id = 1L, party = party, user = null, guestName = "A", guestEmail = "a@a.com")
        val p2 = Participant(id = 2L, party = party, user = null, guestName = "B", guestEmail = "b@b.com")
        
        every { participantRepository.findByPartyId(partyId) } returns listOf(p1, p2)
        every { matchedResultRepository.saveAll(any<List<party.manitto.global.entity.MatchedResult>>()) } answers {
            invocation.args[0] as List<party.manitto.global.entity.MatchedResult>
        }
        every { emailJobRepository.saveAll(any<List<party.manitto.global.entity.EmailJob>>()) } answers {
            invocation.args[0] as List<party.manitto.global.entity.EmailJob>
        }

        // when
        val response = matchService.matchAndSave(partyId)

        // then
        assertEquals("매칭 완료. 이메일 발송 대기열에 등록했습니다.", response.message)
        verify(exactly = 1) { matchedResultRepository.saveAll(any<List<party.manitto.global.entity.MatchedResult>>()) }
        verify(exactly = 1) { emailJobRepository.saveAll(match<List<party.manitto.global.entity.EmailJob>> { it.size == 2 }) }
    }
}
