package party.manitto.domain.match


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import party.manitto.global.entity.MatchedResult
import party.manitto.global.entity.Participant
import party.manitto.global.entity.Party

interface MatchedResultRepository : JpaRepository<MatchedResult, Long> {

    // 특정 파티의 매칭 결과 조회용
    @Query("SELECT m FROM MatchedResult m WHERE m.party.id = :partyId")
    fun findByPartyId(@Param("partyId") partyId: Long): List<MatchedResult>

    fun existsByParty(party: Party): Boolean
    
    // 주는 사람(giver)으로 매칭 결과 조회
    fun findByGiver(giver: Participant): MatchedResult?
}