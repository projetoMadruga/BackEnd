package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.LoginDTO;
import Ouvidoria.Senai.dtos.RecuperarSenhaDTO;
import Ouvidoria.Senai.dtos.RedefinirSenhaDTO;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.TokenRedefinicaoSenha;
import Ouvidoria.Senai.repositories.LoginRepository;
import Ouvidoria.Senai.repositories.TokenRedefinicaoSenhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private TokenRedefinicaoSenhaRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;

    public LoginDTO cadastrarUsuario(LoginDTO dto) {
        // Verifica se email já existe
        if (loginRepository.findByEmailEducacional(dto.getEmailEducacional()) != null) {
            throw new RuntimeException("Email já cadastrado: " + dto.getEmailEducacional());
        }

        // Valida o domínio do email
        String email = dto.getEmailEducacional();
        if (!email.endsWith("@aluno.senai.br") && !email.endsWith("@docente.senai.br")) {
            throw new IllegalArgumentException("Email inválido. Use apenas emails institucionais (@aluno.senai.br ou @docente.senai.br)");
        }

        // Valida o cargo
        if (dto.getCargoUsuario() == null) {
            throw new IllegalArgumentException("Cargo do usuário é obrigatório");
        }

        // Cria e salva novo usuário
        Login novoUsuario = new Login();
        novoUsuario.setEmailEducacional(dto.getEmailEducacional());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        novoUsuario.setCargoUsuario(dto.getCargoUsuario());

        Login usuarioSalvo = loginRepository.save(novoUsuario);

        // Retorna DTO com os dados do usuário cadastrado (sem a senha)
        return new LoginDTO(usuarioSalvo);
    }

    // O método autenticarUsuario() foi REMOVIDO daqui.
    
    public String solicitarRecuperacaoSenha(RecuperarSenhaDTO dto) {
        Login usuario = loginRepository.findByEmailEducacional(dto.getEmailEducacional());
        if (usuario == null) {
            throw new RuntimeException("E-mail não encontrado: " + dto.getEmailEducacional());
        }
        
        // Verificar se já existe um token ativo para este usuário
        Optional<TokenRedefinicaoSenha> tokenExistente = tokenRepository.findByLoginAndUtilizadoFalse(usuario);
        if (tokenExistente.isPresent()) {
            TokenRedefinicaoSenha token = tokenExistente.get();
            // Se o token não estiver expirado, reutiliza-o
            if (!token.isExpirado()) {
                emailService.enviarEmailRecuperacaoSenha(usuario.getEmailEducacional(), token.getToken());
                return "E-mail de recuperação enviado com sucesso!";
            }
        }
        
        // Gerar novo token
        String token = UUID.randomUUID().toString();
        TokenRedefinicaoSenha tokenRedefinicao = new TokenRedefinicaoSenha(
                token,
                usuario,
                LocalDateTime.now().plusHours(1) // Token válido por 1 hora
        );
        
        tokenRepository.save(tokenRedefinicao);
        
        // Enviar e-mail com o token
        emailService.enviarEmailRecuperacaoSenha(usuario.getEmailEducacional(), token);
        
        return "E-mail de recuperação enviado com sucesso!";
    }
    
    public String redefinirSenha(RedefinirSenhaDTO dto) {
        Optional<TokenRedefinicaoSenha> tokenOpt = tokenRepository.findByToken(dto.getToken());
        
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token inválido");
        }
        
        TokenRedefinicaoSenha token = tokenOpt.get();
        
        if (token.isUtilizado()) {
            throw new RuntimeException("Este token já foi utilizado");
        }
        
        if (token.isExpirado()) {
            throw new RuntimeException("Token expirado");
        }
        
        Login usuario = token.getLogin();
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        loginRepository.save(usuario);
        
        // Marcar token como utilizado
        token.setUtilizado(true);
        tokenRepository.save(token);
        
        return "Senha redefinida com sucesso!";
    }
    
    /**
     * Busca um usuário pelo email educacional
     * @param email Email do usuário
     * @return Objeto Login ou null se não encontrado
     */
    @Cacheable(value = "usuarios", key = "#email")
    public Login buscarPorEmail(String email) {
        return loginRepository.findByEmailEducacional(email);
    }
}