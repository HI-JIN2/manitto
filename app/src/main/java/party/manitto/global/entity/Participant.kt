package party.manitto.global.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class Participant(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    val user: User? = null, // 게스트 모드를 위해 nullable

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    val party: Party,

    @Column(nullable = true)
    val nickname: String? = null,

    // 게스트 모드용 필드 (user가 null일 때 사용)
    @Column(nullable = true)
    val guestName: String? = null,

    @Column(nullable = true)
    val guestEmail: String? = null
) {
    // User 정보 접근 헬퍼
    val email: String get() = user?.email ?: guestEmail ?: ""
    val displayName: String get() = nickname ?: user?.name ?: guestName ?: email
    val isGuest: Boolean get() = user == null
}