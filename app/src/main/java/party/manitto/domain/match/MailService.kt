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

        val titlePartyName = partyName ?: "마니또 파티"
        message.subject = "마니또 파티 - 당신의 마니또가 정해졌어요 🎁"

        val body = buildString {
            appendLine("안녕하세요, 마니또 파티입니다.")
            appendLine()
            appendLine("이번 「$titlePartyName」 마니또 파티에서 당신의 마니또가 정해졌어요.")
            appendLine()
            appendLine("🎁 나의 마니또: $target")
            appendLine()
            appendLine("마니또가 들키지 않도록 살짝 마음을 써 주세요.")
            appendLine("작은 선물이나 따뜻한 메시지 하나만으로도 파티는 훨씬 즐거워질 수 있어요.")
            appendLine("서로의 정성과 센스가 모일수록, 오래 기억에 남는 시간이 될 거예요.")
            appendLine()
            appendLine("또한, 이 이메일 주소로 참여한 마니또 파티를 나중에 한 번에 모아보고 싶다면,")
            appendLine("마니또 파티 서비스에 회원 가입해 보세요.")
            appendLine("앞으로는 참여 이력을 계정에서 편하게 확인할 수 있어요.")
            appendLine()
            appendLine("그럼, 즐거운 마니또 파티 되세요 💝")
            appendLine()
            appendLine("— 마니또 파티 팀 드림")
        }

        message.text = body
        mailSender.send(message)
    }
}