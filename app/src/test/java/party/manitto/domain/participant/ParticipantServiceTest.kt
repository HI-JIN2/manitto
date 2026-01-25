package party.manitto.domain.participant

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import party.manitto.domain.match.MatchedResultRepository
import party.manitto.domain.party.PartyRepository
import party.manitto.global.CustomException
import party.manitto.global.ErrorCode
import party.manitto.global.entity.Party
import party.manitto.global.entity.User

@ExtendWith(MockKExtension::class)
class ParticipantServiceTest {

    @MockK
    lateinit var participantRepository: ParticipantRepository

    @MockK
    lateinit var partyRepository: PartyRepository

    @MockK
    lateinit var matchedResultRepository: MatchedResultRepository

    @InjectMockKs
    lateinit var participantService: ParticipantService

    @Test
    fun `joinPartyById - 파티 인원 제한(30명) 초과 시 예외 발생`() {
        // given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party", inviteCode = "ABCDEF", host = null)
        val user = User(id = 10L, email = "u@test.com", name = "user")

        every { partyRepository.findByIdForUpdate(partyId) } returns party
        every { matchedResultRepository.existsByParty(party) } returns false
        every { participantRepository.countByPartyId(partyId) } returns Party.MAX_PARTICIPANTS.toLong()
        every { participantRepository.findByPartyIdAndUser(partyId, user) } returns null

        // when & then
        val exception = assertThrows<CustomException> {
            participantService.joinPartyById(partyId, user)
        }
        assertEquals(ErrorCode.PARTY_PARTICIPANT_LIMIT_EXCEEDED, exception.errorCode)
        verify(exactly = 0) { participantRepository.save(any()) }
    }

    @Test
    fun `joinPartyAsGuest - 파티 인원 제한(30명) 초과 시 예외 발생`() {
        // given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party", inviteCode = "ABCDEF", host = null)

        every { partyRepository.findByIdForUpdate(partyId) } returns party
        every { matchedResultRepository.existsByParty(party) } returns false
        every { participantRepository.countByPartyId(partyId) } returns Party.MAX_PARTICIPANTS.toLong()

        // when & then
        val exception = assertThrows<CustomException> {
            participantService.joinPartyAsGuest(partyId, name = "Guest", email = "guest@test.com")
        }
        assertEquals(ErrorCode.PARTY_PARTICIPANT_LIMIT_EXCEEDED, exception.errorCode)
        verify(exactly = 0) { participantRepository.save(any()) }
    }
}
