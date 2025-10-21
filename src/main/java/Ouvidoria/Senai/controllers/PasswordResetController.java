package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.PasswordForgotDTO;
import Ouvidoria.Senai.dtos.PasswordResetDTO;
import Ouvidoria.Senai.services.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordForgotDTO forgotDTO) {
        passwordResetService.createPasswordResetTokenForUser(forgotDTO.getEmail());
        return ResponseEntity.ok("Se o e-mail estiver cadastrado, um link de recuperação de senha será enviado.");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetDTO resetDTO) {
        try {
            passwordResetService.resetPassword(resetDTO.getToken(), resetDTO.getNewPassword());
            return ResponseEntity.ok("Senha redefinida com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
