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
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setTo(destinatario);
            mensagem.setSubject("Recuperação de Senha - Ouvidoria SENAI");
            mensagem.setText("Olá,\n\n" +
                    "Você solicitou a recuperação de senha para o sistema de Ouvidoria SENAI.\n\n" +
                    "Para redefinir sua senha, utilize o seguinte token: " + token + "\n\n" +
                    "Se você não solicitou esta recuperação, por favor ignore este e-mail.\n\n" +
                    "Atenciosamente,\nEquipe Ouvidoria SENAI");

            mailSender.send(mensagem);
            System.out.println("Email enviado para: " + destinatario + " com token: " + token);
        } catch (Exception e) {
            // Para desenvolvimento: simula envio bem-sucedido mesmo se falhar
            System.out.println("SIMULAÇÃO - Email para: " + destinatario);
            System.out.println("SIMULAÇÃO - Token de recuperação: " + token);
            System.out.println("Erro no envio real: " + e.getMessage());
        }
    }
}