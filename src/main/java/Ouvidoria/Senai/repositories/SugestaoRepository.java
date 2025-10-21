package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.Sugestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SugestaoRepository extends JpaRepository<Sugestao, Long> {

    // Método para buscar todas as sugestões de um usuário específico
    List<Sugestao> findByUsuario(Login usuario);
    
    @Query("SELECT s FROM Sugestao s JOIN FETCH s.usuario WHERE s.id = :id")
    Sugestao findByIdWithUsuario(@Param("id") Long id);
    
    @Query("SELECT s FROM Sugestao s JOIN FETCH s.usuario")
    List<Sugestao> findAllWithUsuario();
    
    @Query("SELECT s FROM Sugestao s JOIN FETCH s.usuario WHERE s.usuario = :usuario")
    List<Sugestao> findByUsuarioWithDetails(@Param("usuario") Login usuario);
}