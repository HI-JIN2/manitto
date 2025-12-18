package party.manitto.domain.user;

import org.springframework.data.jpa.repository.JpaRepository
import party.manitto.global.entity.User

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}