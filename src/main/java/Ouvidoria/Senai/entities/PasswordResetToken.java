package Ouvidoria.Senai.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = Login.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "login_id")
    private Login login;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, Login login) {
        this.token = token;
        this.login = login;
        this.expiryDate = LocalDateTime.now().plusHours(24); // Token válido por 24 horas
    }

    public boolean isTokenValid() {
        return this.expiryDate != null && LocalDateTime.now().isBefore(this.expiryDate);
    }
}
