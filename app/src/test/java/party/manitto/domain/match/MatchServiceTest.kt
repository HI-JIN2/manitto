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
    lateinit var mailService: MailService

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
        every { matchedResultRepository.saveAll(any<List<party.manitto.global.entity.MatchedResult>>()) } returns emptyList()
        every { mailService.sendMatchEmailAsync(any(), any(), any()) } returns Unit

        // when
        val response = matchService.matchAndSave(partyId)

        // then
        assertEquals("매칭 완료 및 이메일 발송 중입니다.", response.message)
        verify(exactly = 1) { matchedResultRepository.saveAll(any<List<party.manitto.global.entity.MatchedResult>>()) }
        // 2명에게 이메일 발송
        verify(exactly = 2) { mailService.sendMatchEmailAsync(any(), any(), any()) }
    }
}
