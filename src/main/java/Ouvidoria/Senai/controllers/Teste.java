package Ouvidoria.Senai.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("teste")
@RestController
public class Teste {

    @GetMapping
    public String mostrar(){
        return "Ola mundo";
    }
}
