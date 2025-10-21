package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.Area;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.PermissaoAreaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissaoAreaUsuarioRepository extends JpaRepository<PermissaoAreaUsuario, Long> {
    Optional<PermissaoAreaUsuario> findByUsuarioAndArea(Login usuario, Area area);
    List<PermissaoAreaUsuario> findByUsuario(Login usuario);
}
