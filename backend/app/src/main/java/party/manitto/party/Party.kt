package party.manitto.party

import jakarta.persistence.*

@Entity
data class Party(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val password: String
)