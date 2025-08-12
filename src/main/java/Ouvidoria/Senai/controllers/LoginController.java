package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.*;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.services.LoginService;
import Ouvidoria.Senai.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private LoginService loginService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarUsuario(@RequestBody @Valid LoginDTO dto) {
        try {
            // Validação básica de entrada
            if (dto.getEmailEducacional() == null || dto.getEmailEducacional().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email é obrigatório.");
            }
            
            if (dto.getSenha() == null || dto.getSenha().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Senha é obrigatória.");
            }
            
            // Validação de formato de email
            if (!dto.getEmailEducacional().matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de email inválido.");
            }
            
            // Validação de força da senha
            if (dto.getSenha().length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A senha deve ter pelo menos 8 caracteres.");
            }
            
            LoginDTO usuarioCadastrado = loginService.cadastrarUsuario(dto);
            // Retorna 201 CREATED com os dados do usuário (sem a senha)
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCadastrado);
        } catch (RuntimeException e) {
            // Retorna 400 BAD REQUEST se o email já existir ou outro erro ocorrer.
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log do erro (em produção, use um logger adequado)
            System.err.println("Erro inesperado no cadastro: " + e.getMessage());
            // Retorna 500 INTERNAL_SERVER_ERROR para erros inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor.");
        }
    }

    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticarUsuario(@RequestBody @Valid LoginDTO dto) {
        try {
            // Validação básica de entrada
            if (dto.getEmailEducacional() == null || dto.getEmailEducacional().trim().isEmpty() ||
                dto.getSenha() == null || dto.getSenha().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email e senha são obrigatórios.");
            }
            
            var authenticationToken = new UsernamePasswordAuthenticationToken(dto.getEmailEducacional(), dto.getSenha());
            var authentication = manager.authenticate(authenticationToken);
            
            Login usuario = (Login) authentication.getPrincipal();
            var tokenJWT = tokenService.gerarToken(usuario);
            var refreshToken = tokenService.gerarRefreshToken(usuario);

            return ResponseEntity.ok(new TokenJWTDTO(tokenJWT, refreshToken));
        } catch (BadCredentialsException e) {
            // Retorna 401 UNAUTHORIZED se as credenciais forem inválidas.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos.");
        } catch (AuthenticationException e) {
            // Retorna 401 UNAUTHORIZED para outros erros de autenticação
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro de autenticação: " + e.getMessage());
        } catch (Exception e) {
            // Log do erro (em produção, use um logger adequado)
            System.err.println("Erro inesperado na autenticação: " + e.getMessage());
            // Retorna 500 INTERNAL_SERVER_ERROR para erros inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor.");
        }
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenDTO dto) {
        try {
            // Verificar se o token não é nulo ou vazio
            if (dto.getRefreshToken() == null || dto.getRefreshToken().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token não fornecido.");
            }
            
            // Verificar se é um refresh token válido
            if (!tokenService.isRefreshToken(dto.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido.");
            }
            
            // Obter o email do usuário a partir do token
            String email = tokenService.validarToken(dto.getRefreshToken());
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expirado ou inválido.");
            }
            
            // Buscar o usuário pelo email
            Login usuario = loginService.buscarPorEmail(email);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado.");
            }
            
            // Gerar novos tokens
            var novoToken = tokenService.gerarToken(usuario);
            var novoRefreshToken = tokenService.gerarRefreshToken(usuario);
            
            return ResponseEntity.ok(new TokenJWTDTO(novoToken, novoRefreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao renovar token: " + e.getMessage());
        }
    }
    
    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody @Valid RecuperarSenhaDTO dto) {
        try {
            // Validação básica de entrada
            if (dto.getEmailEducacional() == null || dto.getEmailEducacional().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email é obrigatório.");
            }
            
            // Validação de formato de email
            if (!dto.getEmailEducacional().matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de email inválido.");
            }
            
            String mensagem = loginService.solicitarRecuperacaoSenha(dto);
            return ResponseEntity.ok(mensagem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log do erro (em produção, use um logger adequado)
            System.err.println("Erro inesperado na recuperação de senha: " + e.getMessage());
            // Retorna 500 INTERNAL_SERVER_ERROR para erros inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor.");
        }
    }
    
    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody @Valid RedefinirSenhaDTO dto) {
        try {
            // Validação básica de entrada
            if (dto.getToken() == null || dto.getToken().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token é obrigatório.");
            }
            
            if (dto.getNovaSenha() == null || dto.getNovaSenha().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nova senha é obrigatória.");
            }
            
            // Validação de força da senha
            if (dto.getNovaSenha().length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A nova senha deve ter pelo menos 8 caracteres.");
            }
            
            String mensagem = loginService.redefinirSenha(dto);
            return ResponseEntity.ok(mensagem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log do erro (em produção, use um logger adequado)
            System.err.println("Erro inesperado na redefinição de senha: " + e.getMessage());
            // Retorna 500 INTERNAL_SERVER_ERROR para erros inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor.");
        }
    }
}