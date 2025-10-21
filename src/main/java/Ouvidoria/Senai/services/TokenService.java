package Ouvidoria.Senai.services;

import Ouvidoria.Senai.entities.Login;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;
    
    @Value("${api.security.token.expiration:7200000}")
    private Long expiration; // Valor em milissegundos (padrão: 2 horas)

    /**
     * Gera um token JWT de acesso para o usuário
     * @param usuario Usuário autenticado
     * @return Token JWT assinado
     */
    public String gerarToken(Login usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("API Ouvidoria SENAI")
                    .withSubject(usuario.getEmailEducacional())
                    .withClaim("id", usuario.getId())
                    .withClaim("role", usuario.getCargoUsuario().name())
                    .withClaim("jti", UUID.randomUUID().toString()) // ID único do token
                    .withIssuedAt(new Date()) // Data de emissão
                    .withExpiresAt(getExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }
    
    /**
     * Gera um refresh token com validade mais longa
     * @param usuario Usuário autenticado
     * @return Refresh token JWT assinado
     */
    public String gerarRefreshToken(Login usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("API Ouvidoria SENAI")
                    .withSubject(usuario.getEmailEducacional())
                    .withClaim("refresh", true)
                    .withClaim("jti", UUID.randomUUID().toString())
                    .withIssuedAt(new Date())
                    .withExpiresAt(getRefreshTokenExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar refresh token", exception);
        }
    }

    /**
     * Valida um token JWT e retorna o subject (email do usuário)
     * @param token Token JWT a ser validado
     * @return Email do usuário ou null se inválido
     */
    public String validarToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("API Ouvidoria SENAI")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null; // Token inválido
        }
    }
    
    /**
     * Verifica se um token é um refresh token válido
     * @param token Token JWT a ser validado
     * @return true se for um refresh token válido, false caso contrário
     */
    public boolean isRefreshToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("API Ouvidoria SENAI")
                    .build()
                    .verify(token)
                    .getClaim("refresh").asBoolean();
        } catch (JWTVerificationException exception) {
            return false;
        }
    }
    
    /**
     * Obtém o ID do usuário a partir do token
     * @param token Token JWT válido
     * @return ID do usuário ou null se não encontrado
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("API Ouvidoria SENAI")
                    .build()
                    .verify(token)
                    .getClaim("id").asLong();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    /**
     * Calcula a data de expiração do token de acesso
     * @return Data de expiração como Instant
     */
    private Instant getExpirationDate() {
        return Instant.now().plusMillis(expiration);
    }
    
    /**
     * Calcula a data de expiração do refresh token (15 dias)
     * @return Data de expiração como Instant
     */
    private Instant getRefreshTokenExpirationDate() {
        return Instant.now().plusMillis(15 * 24 * 60 * 60 * 1000L); // 15 dias
    }
}