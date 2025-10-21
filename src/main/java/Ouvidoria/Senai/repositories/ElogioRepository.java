package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.Elogio;
import Ouvidoria.Senai.entities.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ElogioRepository extends JpaRepository<Elogio, Long> {

    List<Elogio> findByUsuario(Login usuario);
    
    @Query("SELECT e FROM Elogio e JOIN FETCH e.usuario WHERE e.id = :id")
    Elogio findByIdWithUsuario(@Param("id") Long id);
    
    @Query("SELECT e FROM Elogio e JOIN FETCH e.usuario")
    List<Elogio> findAllWithUsuario();
    
    @Query("SELECT e FROM Elogio e JOIN FETCH e.usuario WHERE e.usuario = :usuario")
    List<Elogio> findByUsuarioWithDetails(@Param("usuario") Login usuario);
}
