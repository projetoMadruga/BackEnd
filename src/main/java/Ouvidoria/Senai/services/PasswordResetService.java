package Ouvidoria.Senai.services;

import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.PasswordResetToken;
import Ouvidoria.Senai.repositories.LoginRepository;
import Ouvidoria.Senai.repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        Login login = loginRepository.findFirstByEmailEducacional(email);
        if (login == null) {
            // Lançar exceção ou tratar o caso de e-mail não encontrado
            // Por segurança, pode ser melhor não informar que o e-mail não existe
            System.out.println("Tentativa de recuperação de senha para e-mail não cadastrado: " + email);
            return;
        }

        // Invalida tokens anteriores para o mesmo usuário
        tokenRepository.deleteByLoginId(login.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, login);
        tokenRepository.save(myToken);

        emailService.enviarEmailRecuperacaoSenha(login.getEmailEducacional(), token);
    }

    public void validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = tokenRepository.findByToken(token);
        if (passToken == null || !passToken.isTokenValid()) {
            throw new RuntimeException("Token inválido ou expirado");
        }
    }

    @Transactional
    public void changeUserPassword(String token, String newPassword) {
        PasswordResetToken passToken = tokenRepository.findByToken(token);
        if (passToken == null || !passToken.isTokenValid()) {
            throw new RuntimeException("Token inválido ou expirado");
        }

        Login login = passToken.getLogin();
        login.setSenha(passwordEncoder.encode(newPassword));
        loginRepository.save(login);
        tokenRepository.delete(passToken);
    }

    public void resetPassword(String token, String newPassword) {
        changeUserPassword(token, newPassword);
    }
}
