package Ouvidoria.Senai.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetDTO {

    @NotBlank(message = "O token não pode ser vazio.")
    private String token;

    @NotBlank(message = "A nova senha não pode ser vazia.")
    private String newPassword;
}
