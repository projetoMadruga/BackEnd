package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.Area;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.UserAreaPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAreaPermissionRepository extends JpaRepository<UserAreaPermission, Long> {
    List<UserAreaPermission> findByUser(Login user);
    Optional<UserAreaPermission> findByUserAndArea(Login user, Area area);
}
