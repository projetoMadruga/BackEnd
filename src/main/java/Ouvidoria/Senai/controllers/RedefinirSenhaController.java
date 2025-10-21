package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.RedefinirSenhaDTO;
import Ouvidoria.Senai.services.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/redefinir-senha")
public class RedefinirSenhaController {

    @Autowired
    private LoginService loginService;

    @GetMapping
    public String mostrarPaginaRedefinicao(@RequestParam("token") String token, Model model) {
        try {
            // Verifica se o token é válido
            boolean tokenValido = loginService.validarTokenRedefinicao(token);
            if (!tokenValido) {
                model.addAttribute("erro", "Token inválido ou expirado. Por favor, solicite uma nova redefinição de senha.");
                return "erro";
            }
            
            // Prepara o DTO para o formulário
            RedefinirSenhaDTO redefinirSenhaDTO = new RedefinirSenhaDTO(token, "");
            model.addAttribute("redefinirSenhaDTO", redefinirSenhaDTO);
            return "redefinir-senha";
        } catch (Exception e) {
            model.addAttribute("erro", "Ocorreu um erro ao processar sua solicitação: " + e.getMessage());
            return "erro";
        }
    }

    @PostMapping
    public String redefinirSenha(@ModelAttribute RedefinirSenhaDTO dto, Model model) {
        try {
            String mensagem = loginService.redefinirSenha(dto);
            model.addAttribute("mensagem", mensagem);
            return "senha-redefinida";
        } catch (RuntimeException e) {
            model.addAttribute("erro", e.getMessage());
            return "erro";
        } catch (Exception e) {
            model.addAttribute("erro", "Ocorreu um erro ao redefinir sua senha. Por favor, tente novamente.");
            return "erro";
        }
    }
}
