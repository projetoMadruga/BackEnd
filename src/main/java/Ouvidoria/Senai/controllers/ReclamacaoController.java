package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.ReclamacaoDTO;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.services.ReclamacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reclamacoes")
@CrossOrigin(origins = "*")
public class ReclamacaoController {

    @Autowired
    private ReclamacaoService reclamacaoService; // 3. Injeção corrigida

    @PostMapping
    public ResponseEntity<ReclamacaoDTO> criarReclamacao(@RequestBody @Valid ReclamacaoDTO reclamacaoDTO) {
        ReclamacaoDTO resposta = reclamacaoService.salvarReclamacao(reclamacaoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<ReclamacaoDTO>> listarReclamacoes() {
        List<ReclamacaoDTO> lista = reclamacaoService.listarManifestacoes(); // 6. Chamada de método corrigida
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReclamacaoDTO> buscarReclamacaoPorId(@PathVariable Long id) {
        try {
            ReclamacaoDTO dto = reclamacaoService.buscarPorId(id); // 7. Chamada de método corrigida
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) { // 8. ORDEM CORRIGIDA: Específico primeiro
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) { // 9. ORDEM CORRIGIDA: Outro específico depois
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<ReclamacaoDTO> atualizarReclamacao(@PathVariable Long id,
                                                     @RequestBody ReclamacaoDTO dto) {
        try {
            ReclamacaoDTO resposta = reclamacaoService.atualizarReclamacao(id, dto);
            return ResponseEntity.ok(resposta);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarReclamacao(@PathVariable Long id) {
        try {
            reclamacaoService.deletarReclamacao(id);
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