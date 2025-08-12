package Ouvidoria.Senai.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tb_login")
public class Login implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String emailEducacional;
    private String senha;

    @Enumerated(EnumType.STRING)
    private CargoUsuario cargoUsuario;

    // Construtores, Getters e Setters

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna o cargo do usuário como uma autoridade para o Spring Security
        return List.of(new SimpleGrantedAuthority(cargoUsuario.name()));
    }

    public Login() {

    }

    public Login(Long id, String emailEducacional, String senha, CargoUsuario cargoUsuario) {
        this.id = id;
        this.emailEducacional = emailEducacional;
        this.senha = senha;
        this.cargoUsuario = cargoUsuario;
    }


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

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.emailEducacional;
    }

    // Para simplificar, vamos retornar true para os métodos abaixo.
    // Você pode implementar lógicas mais complexas aqui se necessário (ex: conta expirada, etc.).
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}