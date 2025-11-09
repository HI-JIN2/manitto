package party.manitto.domain.match

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailSender: JavaMailSender
) {
    fun sendMatchEmail(to: String, target: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = "[마니또] 당신의 마니또가 정해졌습니다 🎁"
        message.text = "안녕하세요! 당신의 마니또는 ${target} 입니다."
        mailSender.send(message)

    }
}