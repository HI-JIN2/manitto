package party.manitto.domain.participant

import party.manitto.global.entity.Participant
import party.manitto.global.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ParticipantRepository : JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.party.id = :partyId AND p.user = :user")
    fun findByPartyIdAndUser(
        @Param("partyId") partyId: Long,
        @Param("user") user: User
    ): Participant?

    @Query("SELECT p FROM Participant p WHERE p.party.id = :partyId")
    fun findByPartyId(@Param("partyId") partyId: Long): List<Participant>

    @Query("SELECT p FROM Participant p WHERE p.user = :user")
    fun findByUser(@Param("user") user: User): List<Participant>
}