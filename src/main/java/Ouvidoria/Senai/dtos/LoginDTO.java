package Ouvidoria.Senai.dtos;

import Ouvidoria.Senai.entities.CargoUsuario;
import Ouvidoria.Senai.entities.Login;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginDTO {
    private Long id;
    
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")

    private String emailEducacional;
    
    @NotBlank(message = "A senha é obrigatória")

    private String senha;
    
    private CargoUsuario cargoUsuario;

    // Construtores
    public LoginDTO() {
    }

    public LoginDTO(Long id, String emailEducacional, String senha, CargoUsuario cargoUsuario) {
        this.id = id;
        this.emailEducacional = emailEducacional;
        this.senha = senha;
        this.cargoUsuario = cargoUsuario;
    }

    public LoginDTO(Login login) {
        this.id = login.getId();
        this.emailEducacional = login.getEmailEducacional();
        this.senha = login.getSenha();
        this.cargoUsuario = login.getCargoUsuario();
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailEducacional() {
        return emailEducacional;
    }

    public void setEmailEducacional(String emailEducacional) {
        this.emailEducacional = emailEducacional;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public CargoUsuario getCargoUsuario() {
        return cargoUsuario;
    }

    public void setCargoUsuario(CargoUsuario cargoUsuario) {
        this.cargoUsuario = cargoUsuario;
    }
}