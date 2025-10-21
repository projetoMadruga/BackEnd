package Ouvidoria.Senai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.reset-password-url-base:http://localhost:8080/redefinir-senha}")
    private String resetPasswordUrlBase;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.mail.simulate:false}")
    private boolean simulate;

    public void enviarEmailRecuperacaoSenha(String destinatario, String token) {
        try {
            // Construindo a URL de redefinição de senha
            String base = resetPasswordUrlBase.endsWith("/") ? resetPasswordUrlBase : resetPasswordUrlBase + "/";
            String urlRedefinicao = base + "redefinir-senha?token=" + token;

            if (simulate) {
                System.out.println("[SIMULAÇÃO] Email de recuperação NÃO enviado via SMTP.");
                System.out.println("[SIMULAÇÃO] Destinatário: " + destinatario);
                System.out.println("[SIMULAÇÃO] URL de redefinição: " + urlRedefinicao);
                System.out.println("[SIMULAÇÃO] Token: " + token);
                return;
            }

            Context context = new Context();
            context.setVariable("urlRedefinicao", urlRedefinicao);

            String htmlContent = templateEngine.process("recuperacao-senha", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Recuperação de Senha - Ouvidoria SENAI");
            helper.setText(htmlContent, true);
            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }

            mailSender.send(mimeMessage);
            System.out.println("Email de recuperação enviado para: " + destinatario);

        } catch (Exception e) {
            System.err.println("Erro ao enviar email para: " + destinatario);
            System.err.println("Erro: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail de recuperação de senha. Por favor, tente novamente mais tarde.", e);
        }
    }
}