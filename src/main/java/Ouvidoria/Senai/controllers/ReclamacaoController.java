package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.ReclamacaoDTO;
import Ouvidoria.Senai.entities.StatusReclamacao;
import Ouvidoria.Senai.entities.TipoReclamacao;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.services.ReclamacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public ResponseEntity<List<ReclamacaoDTO>> listarReclamacoes(
            @RequestParam(required = false) TipoReclamacao tipo,
            Authentication authentication) {
        List<ReclamacaoDTO> lista = reclamacaoService.listarManifestacoes();
        
        // Se o tipo for especificado e o usuário for ADMIN ou MANUTENCAO, filtra por tipo
        if (tipo != null && (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
                authentication.getAuthorities().contains(new SimpleGrantedAuthority("MANUTENCAO")))) {
            lista = lista.stream()
                    .filter(r -> r.getTipoReclamacao() == tipo)
                    .toList();
        }
        
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
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReclamacaoDTO> atualizarStatusReclamacao(
            @PathVariable Long id,
            @RequestParam StatusReclamacao status,
            @RequestParam(required = false) String observacao,
            Authentication authentication) {
        try {
            // Verifica se o usuário tem permissão (ADMIN ou MANUTENCAO)
            boolean isAuthorized = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ||
                    authentication.getAuthorities().contains(new SimpleGrantedAuthority("MANUTENCAO"));
            
            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Busca a reclamação atual
            ReclamacaoDTO reclamacaoAtual = reclamacaoService.buscarPorId(id);
            
            // Atualiza o status e observação
            reclamacaoAtual.setStatus(status);
            if (observacao != null && !observacao.isEmpty()) {
                reclamacaoAtual.setObservacao(observacao);
            }
            
            // Salva as alterações
            ReclamacaoDTO reclamacaoAtualizada = reclamacaoService.atualizarReclamacao(id, reclamacaoAtual);
            return ResponseEntity.ok(reclamacaoAtualizada);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}