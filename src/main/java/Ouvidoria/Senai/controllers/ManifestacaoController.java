package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.ManifestacaoUnificadaDTO;
import Ouvidoria.Senai.services.ManifestacaoUnificadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manifestacoes")
@CrossOrigin(origins = "*")
public class ManifestacaoController {

    @Autowired
    private ManifestacaoUnificadaService manifestacaoUnificadaService;

    /**
     * Lista todas as manifestações do usuário logado ou todas se for ADMIN
     */
    @GetMapping
    public ResponseEntity<List<ManifestacaoUnificadaDTO>> listarManifestacoes(Authentication authentication) {
        try {
            List<ManifestacaoUnificadaDTO> manifestacoes = manifestacaoUnificadaService.listarManifestacoes(authentication);
            return ResponseEntity.ok(manifestacoes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lista manifestações por tipo (para admins)
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ManifestacaoUnificadaDTO>> listarManifestacoesPorTipo(
            @PathVariable String tipo,
            Authentication authentication) {
        
        // Verifica se é admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        try {
            List<ManifestacaoUnificadaDTO> manifestacoes = manifestacaoUnificadaService.listarManifestacoesPorTipo(tipo);
            return ResponseEntity.ok(manifestacoes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Busca uma manifestação específica por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ManifestacaoUnificadaDTO> buscarManifestacaoPorId(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            ManifestacaoUnificadaDTO manifestacao = manifestacaoUnificadaService.buscarPorId(id, authentication);
            return ResponseEntity.ok(manifestacao);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza uma manifestação (apenas para admins)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ManifestacaoUnificadaDTO> atualizarManifestacao(
            @PathVariable Long id,
            @RequestBody ManifestacaoUnificadaDTO dto,
            Authentication authentication) {
        
        // Verifica se é admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        try {
            ManifestacaoUnificadaDTO manifestacaoAtualizada = manifestacaoUnificadaService.atualizarManifestacao(id, dto, authentication);
            return ResponseEntity.ok(manifestacaoAtualizada);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deleta uma manifestação (apenas para admins)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarManifestacao(
            @PathVariable Long id,
            Authentication authentication) {
        
        // Verifica se é admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        try {
            manifestacaoUnificadaService.deletarManifestacao(id, authentication);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
