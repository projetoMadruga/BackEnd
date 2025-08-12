package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.Reclamacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReclamacaoRepository extends JpaRepository<Reclamacao, Long> {

    List<Reclamacao> findByUsuario(Login usuario);
    
    @Query("SELECT r FROM Reclamacao r JOIN FETCH r.usuario WHERE r.id = :id")
    Reclamacao findByIdWithUsuario(@Param("id") Long id);
    
    @Query("SELECT r FROM Reclamacao r JOIN FETCH r.usuario")
    List<Reclamacao> findAllWithUsuario();
    
    @Query("SELECT r FROM Reclamacao r JOIN FETCH r.usuario WHERE r.usuario = :usuario")
    List<Reclamacao> findByUsuarioWithDetails(@Param("usuario") Login usuario);
}
