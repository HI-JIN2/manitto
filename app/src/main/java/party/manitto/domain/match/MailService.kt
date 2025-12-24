package party.manitto.domain.match

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailSender: JavaMailSender
) {
    fun sendMatchEmail(to: String, target: String, partyName: String? = null) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(to)
        
        val titlePartyName = partyName ?: "마니또 파티"
        helper.setSubject("마니또 파티 - 당신의 마니또가 정해졌어요 🎁")

        val htmlBody = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("<meta charset=\"UTF-8\">")
            appendLine("<style>")
            appendLine("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #333; }")
            appendLine(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
            appendLine(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }")
            appendLine(".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }")
            appendLine(".highlight { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 4px; }")
            appendLine(".button { display: inline-block; background: #667eea; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin: 20px 0; font-weight: bold; }")
            appendLine(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 14px; }")
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("<div class=\"container\">")
            appendLine("<div class=\"header\">")
            appendLine("<h1 style=\"margin: 0; font-size: 24px;\">🎁 마니또 파티</h1>")
            appendLine("</div>")
            appendLine("<div class=\"content\">")
            appendLine("<p>안녕하세요,</p>")
            appendLine("<p>마니또 파티의 주인장 페니예요 🐧</p>")
            appendLine("<p>이번 「$titlePartyName」 마니또 파티에서</p>")
            appendLine("<p>당신의 마니또가 정해졌어요.</p>")
            appendLine("<div class=\"highlight\">")
            appendLine("<p style=\"margin: 0; font-size: 18px; font-weight: bold;\">🎁 나의 마니또: <span style=\"color: #667eea;\">$target</span></p>")
            appendLine("</div>")
            appendLine("<p>마니또에게 들키지 않도록</p>")
            appendLine("<p>살짝 마음을 써 주셔도 좋아요.</p>")
            appendLine("<p>작은 선물이나 따뜻한 메시지 하나만으로도</p>")
            appendLine("<p>파티는 훨씬 즐거워질 수 있어요.</p>")
            appendLine("<p>서로의 정성과 센스가 모일수록</p>")
            appendLine("<p>이번 파티도 오래 기억에 남는 시간이 될 거예요.</p>")
            appendLine("<p>혹시 이 이메일 주소로 참여한</p>")
            appendLine("<p>마니또 파티들을 나중에 한 번에 모아보고 싶다면,</p>")
            appendLine("<p>마니또 파티 서비스에 회원 가입해 보세요.</p>")
            appendLine("<p>앞으로는 참여 이력을 계정에서 편하게 확인할 수 있어요.</p>")
            appendLine("<p style=\"text-align: center; margin: 20px 0;\">")
            appendLine("<a href=\"https://manitto-frontend.vercel.app/\" style=\"color: #667eea; text-decoration: none;\">👉 https://manitto-frontend.vercel.app/</a>")
            appendLine("</p>")
            appendLine("<div style=\"text-align: center;\">")
            appendLine("<a href=\"https://manitto-frontend.vercel.app/\" class=\"button\">서비스 바로가기</a>")
            appendLine("</div>")
            appendLine("<p style=\"text-align: center; margin-top: 30px;\">그럼,</p>")
            appendLine("<p style=\"text-align: center;\">즐거운 마니또 파티 되세요 💝</p>")
            appendLine("</div>")
            appendLine("<div class=\"footer\">")
            appendLine("<p>— 페니 드림 🐧</p>")
            appendLine("</div>")
            appendLine("</div>")
            appendLine("</body>")
            appendLine("</html>")
        }

        // 일반 텍스트 버전도 제공 (HTML을 지원하지 않는 클라이언트용)
        val textBody = buildString {
            appendLine("안녕하세요,")
            appendLine()
            appendLine("마니또 파티의 주인장 페니예요 🐧")
            appendLine()
            appendLine("이번 「$titlePartyName」 마니또 파티에서")
            appendLine("당신의 마니또가 정해졌어요.")
            appendLine()
            appendLine("🎁 나의 마니또: $target")
            appendLine()
            appendLine("마니또에게 들키지 않도록")
            appendLine("살짝 마음을 써 주셔도 좋아요.")
            appendLine()
            appendLine("작은 선물이나 따뜻한 메시지 하나만으로도")
            appendLine("파티는 훨씬 즐거워질 수 있어요.")
            appendLine()
            appendLine("서로의 정성과 센스가 모일수록")
            appendLine("이번 파티도 오래 기억에 남는 시간이 될 거예요.")
            appendLine()
            appendLine("혹시 이 이메일 주소로 참여한")
            appendLine("마니또 파티들을 나중에 한 번에 모아보고 싶다면,")
            appendLine("마니또 파티 서비스에 회원 가입해 보세요.")
            appendLine()
            appendLine("앞으로는 참여 이력을 계정에서 편하게 확인할 수 있어요.")
            appendLine()
            appendLine("서비스 바로가기")
            appendLine("👉 https://manitto-frontend.vercel.app/")
            appendLine()
            appendLine("그럼,")
            appendLine("즐거운 마니또 파티 되세요 💝")
            appendLine()
            appendLine("페니 드림 🐧")
        }

        helper.setText(textBody, htmlBody)
        mailSender.send(message)
    }
}