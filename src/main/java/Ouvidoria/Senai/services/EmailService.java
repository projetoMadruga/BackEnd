package Ouvidoria.Senai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEmailRecuperacaoSenha(String destinatario, String token) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Recuperação de Senha - Ouvidoria SENAI");
        mensagem.setText("Olá,\n\n" +
                "Você solicitou a recuperação de senha para o sistema de Ouvidoria SENAI.\n\n" +
                "Para redefinir sua senha, utilize o seguinte token: " + token + "\n\n" +
                "Se você não solicitou esta recuperação, por favor ignore este e-mail.\n\n" +
                "Atenciosamente,\nEquipe Ouvidoria SENAI");

        mailSender.send(mensagem);
    }
}