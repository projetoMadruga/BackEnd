package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.DenunciaDTO;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.services.DenunciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/denuncias")
@CrossOrigin(origins = "*")
public class DenunciaController {

	@Autowired
	private DenunciaService denunciaService; // Serviço correto

	// Endpoint para CRIAR uma nova denúncia
	@PostMapping
	public ResponseEntity<DenunciaDTO> criarDenuncia(@RequestBody DenunciaDTO dto) {
		try {
			// Validação dos campos obrigatórios
			if (dto.getLocal() == null || dto.getLocal().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(new DenunciaDTO(null, null, null, "O assunto não pode estar em branco.", null, null));
			}
			if (dto.getDescricaoDetalhada() == null || dto.getDescricaoDetalhada().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(new DenunciaDTO(null, null, null, "A descrição detalhada é obrigatória.", null, null));
			}
			
			// Chama o método no serviço
			DenunciaDTO resposta = denunciaService.salvarDenuncia(dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
		} catch (Exception e) {
			// É bom logar o erro aqui
			return ResponseEntity.badRequest().build();
		}
	}

	// Endpoint para LISTAR denúncias (do usuário logado ou todas, se for ADMIN)
	@GetMapping
	public ResponseEntity<List<DenunciaDTO>> listarDenuncias() {
		List<DenunciaDTO> lista = denunciaService.listarManifestacoes();
		return ResponseEntity.ok(lista);
	}

	// Endpoint para BUSCAR uma denúncia específica por ID
	@GetMapping("/{id}")
	public ResponseEntity<DenunciaDTO> buscarDenunciaPorId(@PathVariable Long id) {
		try {
			DenunciaDTO dto = denunciaService.buscarPorId(id);
			return ResponseEntity.ok(dto);
		} catch (SecurityException e) { // 1. O específico vem primeiro
			// Este erro acontece quando o usuário não é dono nem ADMIN
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (RuntimeException e) { // 2. O genérico vem por último
			// Este erro acontece quando a denúncia não é encontrada no banco
			return ResponseEntity.notFound().build();
		}
	}
	@PutMapping("/{id}")
	public ResponseEntity<DenunciaDTO> atualizarDenuncia(@PathVariable Long id,
													 @RequestBody DenunciaDTO dto) {
		try {
			DenunciaDTO resposta = denunciaService.atualizarDenuncia(id, dto);
			return ResponseEntity.ok(resposta);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarElogio(@PathVariable Long id) {
		try {
			denunciaService.deletarDenuncia(id);
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