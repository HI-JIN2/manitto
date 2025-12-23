package party.manitto.domain.match

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailSender: JavaMailSender
) {
    fun sendMatchEmail(to: String, target: String, partyName: String? = null) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = "[Manitto] Your Manitto has been assigned! 🎁"
        val partyInfo = if (partyName != null) "\n\nParty: $partyName" else ""
        message.text = "Hello! Your Manitto is ${target}.${partyInfo}\n\nPlease prepare a thoughtful gift! 💝"
        mailSender.send(message)
    }
}