package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.TokenRedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {
    Optional<TokenRedefinicaoSenha> findByToken(String token);
    Optional<TokenRedefinicaoSenha> findByLoginAndUtilizadoFalse(Login login);
}