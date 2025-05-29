package com.project.me.authjavaservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public boolean sendVerificationCode(String emailTo, String verificationCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailTo);
            helper.setFrom("confirmation@reflexia-ai.ru");
            helper.setSubject("Подтверждение email");
            helper.setText(buildEmailContent(verificationCode), true);

            javaMailSender.send(message);
            return true;
        } catch (MessagingException e) {
            log.error("EmailService. Ошибка отправки email подтверждения: {}", e.getMessage());
            return false;
        }
    }

    private String buildEmailContent(String confirmationCode) {
        return """
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        padding: 20px;
                        border-radius: 10px;
                        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        text-align: center;
                        font-size: 24px;
                        color: #333333;
                    }
                    .message {
                        font-size: 16px;
                        color: #555555;
                        margin: 20px 0;
                    }
                    .code {
                        display: block;
                        text-align: center;
                        font-size: 24px;
                        font-weight: bold;
                        color: #1a73e8;
                        margin: 20px 0;
                        padding: 10px;
                        background: #f0f4ff;
                        border-radius: 5px;
                    }
                    .footer {
                        text-align: center;
                        font-size: 14px;
                        color: #aaaaaa;
                        margin-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        Подтверждение электронной почты
                    </div>
                    <div class="message">
                        Здравствуйте! Пожалуйста, используйте код ниже для подтверждения вашей электронной почты.
                    </div>
                    <div class="code">%s</div>
                    <div class="message">
                        Если вы не запрашивали подтверждение, просто проигнорируйте это письмо.
                    </div>
                    <div class="footer">
                        © 2025 Reflexia. Все права защищены.
                    </div>
                </div>
            </body>
            </html>
            """.formatted(confirmationCode.trim());
    }
}
