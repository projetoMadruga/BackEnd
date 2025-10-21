package Ouvidoria.Senai.dtos;

public class TokenJWTDTO {
    private String token;
    private String refreshToken;
    private String tipo = "Bearer";
    
    public TokenJWTDTO(String token) {
        this.token = token;
    }
    
    public TokenJWTDTO(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public String getTipo() {
        return tipo;
    }
}