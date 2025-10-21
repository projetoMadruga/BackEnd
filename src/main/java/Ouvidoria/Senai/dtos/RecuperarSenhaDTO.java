package Ouvidoria.Senai.dtos;

public class RecuperarSenhaDTO {
    private String emailEducacional;

    public RecuperarSenhaDTO() {
    }

    public RecuperarSenhaDTO(String emailEducacional) {
        this.emailEducacional = emailEducacional;
    }

    public String getEmailEducacional() {
        return emailEducacional;
    }

    public void setEmailEducacional(String emailEducacional) {
        this.emailEducacional = emailEducacional;
    }
}