package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.SugestaoDTO;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.services.SugestaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sugestoes")
@CrossOrigin(origins = "*")
public class SugestaoController {

    @Autowired
    private SugestaoService sugestaoService;

    @PostMapping
    public ResponseEntity<SugestaoDTO> criarSugestao(@RequestBody @Valid SugestaoDTO sugestaoDTO) {
        SugestaoDTO resposta = sugestaoService.salvarSugestao(sugestaoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<SugestaoDTO>> listarSugestoes() {
        List<SugestaoDTO> lista = sugestaoService.listarManifestacoes();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SugestaoDTO> buscarSugestaoPorId(@PathVariable Long id) {
        try {
            SugestaoDTO dto = sugestaoService.buscarPorId(id);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) { // 1. O específico (acesso negado) vem primeiro.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) { // 2. O outro específico (não encontrado) vem depois.
            return ResponseEntity.notFound().build();
        }
        // Nota: Se você ainda quisesse pegar um RuntimeException genérico, ele viria por último.
    }
    @PutMapping("/{id}")
    public ResponseEntity<SugestaoDTO> atualizarSugestao(@PathVariable Long id,
                                                     @RequestBody SugestaoDTO dto) {
        try {
            SugestaoDTO resposta = sugestaoService.atualizarSugestao(id, dto);
            return ResponseEntity.ok(resposta);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSugestao(@PathVariable Long id) {
        try {
            sugestaoService.deletarSugestao(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}