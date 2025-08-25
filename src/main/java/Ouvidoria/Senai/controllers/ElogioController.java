package Ouvidoria.Senai.controllers;

import Ouvidoria.Senai.dtos.ElogioDTO;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.services.ElogioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/elogios")
@CrossOrigin(origins = "*")
public class ElogioController {

	@Autowired
	private ElogioService elogioService;

	@PostMapping
	public ResponseEntity<ElogioDTO> criarElogio(@RequestBody ElogioDTO dto) {
		ElogioDTO resposta = elogioService.salvarElogio(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
	}

	@GetMapping
	public ResponseEntity<List<ElogioDTO>> listarElogios() {
		List<ElogioDTO> lista = elogioService.listarManifestacoes();
		return ResponseEntity.ok(lista);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ElogioDTO> buscarElogioPorId(@PathVariable Long id) {
		try {
			ElogioDTO dto = elogioService.buscarPorId(id);
			return ResponseEntity.ok(dto);
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<ElogioDTO> atualizarElogio(@PathVariable Long id,
											 @RequestBody ElogioDTO dto) {
		try {
			ElogioDTO resposta = elogioService.atualizarElogio(id, dto);
			return ResponseEntity.ok(resposta);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (SecurityException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarElogio(@PathVariable Long id) {
		try {
			elogioService.deletarElogio(id);
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
