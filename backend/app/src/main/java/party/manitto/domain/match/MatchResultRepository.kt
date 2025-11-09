package party.manitto.domain.match


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import party.manitto.global.entity.MatchedResult

interface MatchedResultRepository : JpaRepository<MatchedResult, Long> {

    // 특정 파티의 매칭 결과 조회용 (지금은 내부용)
    @Query("SELECT m FROM MatchedResult m WHERE m.party.id = :partyId")
    fun findByPartyId(@Param("partyId") partyId: Long): List<MatchedResult>
}