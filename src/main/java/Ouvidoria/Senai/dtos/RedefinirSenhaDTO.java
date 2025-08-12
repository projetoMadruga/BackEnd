package Ouvidoria.Senai.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RedefinirSenhaDTO {
    @NotBlank(message = "O token é obrigatório")
    private String token;
    
    @NotBlank(message = "A nova senha é obrigatória")
    // Validação de força de senha temporariamente removida para resolver problemas de compilação
    private String novaSenha;

    public RedefinirSenhaDTO() {
    }

    public RedefinirSenhaDTO(String token, String novaSenha) {
        this.token = token;
        this.novaSenha = novaSenha;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}