package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.Denuncia;
import Ouvidoria.Senai.entities.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    List<Denuncia> findByUsuario(Login usuario);
    
    @Query("SELECT d FROM Denuncia d JOIN FETCH d.usuario WHERE d.id = :id")
    Denuncia findByIdWithUsuario(@Param("id") Long id);
    
    @Query("SELECT d FROM Denuncia d JOIN FETCH d.usuario")
    List<Denuncia> findAllWithUsuario();
    
    @Query("SELECT d FROM Denuncia d JOIN FETCH d.usuario WHERE d.usuario = :usuario")
    List<Denuncia> findByUsuarioWithDetails(@Param("usuario") Login usuario);
}
