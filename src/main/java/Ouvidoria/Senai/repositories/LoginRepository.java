package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.CargoUsuario;
import Ouvidoria.Senai.entities.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Login findByEmailEducacional(String emailEducacional);

    List<Login> findByCargoUsuario(CargoUsuario cargoUsuario);
}

