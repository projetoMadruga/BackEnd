package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.DenunciaDTO;
import Ouvidoria.Senai.entities.Denuncia;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.DenunciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DenunciaService {

    @Autowired
    private DenunciaRepository denunciaRepository;

    public DenunciaDTO salvarDenuncia(DenunciaDTO dto) {
        // Pega o usuário logado do contexto de segurança
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Cria a entidade Denuncia
        Denuncia denuncia = new Denuncia();
        denuncia.setLocal(dto.getLocal());
        denuncia.setDataHora(dto.getDataHora());
        denuncia.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        denuncia.setUsuario(usuarioLogado); // Associa a denúncia ao usuário logado

        // Salva no banco
        Denuncia denunciaSalva = denunciaRepository.save(denuncia);

        // Retorna um DTO preenchido com os dados da denúncia salva (incluindo o ID)
        return new DenunciaDTO(denunciaSalva);
    }

    // Métodos de busca otimizados com JOIN FETCH
    public DenunciaDTO buscarPorId(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Usa a consulta otimizada com JOIN FETCH
        Denuncia denuncia = denunciaRepository.findByIdWithUsuario(id);
        if (denuncia == null) {
            throw new ResourceNotFoundException("Denúncia não encontrada. ID: " + id);
        }

        boolean isOwner = denuncia.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isOwner || isAdmin) {
            return new DenunciaDTO(denuncia);
        } else {
            throw new SecurityException("Acesso negado.");
        }
    }

    public List<DenunciaDTO> listarManifestacoes() {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Denuncia> denuncias;
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            // Usa a consulta otimizada com JOIN FETCH para todas as denúncias
            denuncias = denunciaRepository.findAllWithUsuario();
        } else {
            // Usa a consulta otimizada com JOIN FETCH para denúncias do usuário
            denuncias = denunciaRepository.findByUsuarioWithDetails(usuarioLogado);
        }

        return denuncias.stream().map(DenunciaDTO::new).collect(Collectors.toList());
    }
    public DenunciaDTO atualizarDenuncia(Long id, DenunciaDTO dto) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca a denúncia existente usando JOIN FETCH
        Denuncia denunciaExistente = denunciaRepository.findByIdWithUsuario(id);
        if (denunciaExistente == null) {
            throw new ResourceNotFoundException("Denúncia não encontrada. ID: " + id);
        }

        // Verifica permissões
        boolean isOwner = denunciaExistente.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para atualizar este Denuncia.");
        }

        // Atualiza os campos
        denunciaExistente.setDataHora(dto.getDataHora());
        denunciaExistente.setLocal(dto.getLocal());
        denunciaExistente.setDescricaoDetalhada(dto.getDescricaoDetalhada());

        // Salva as alterações
        Denuncia denunciaAtualizado = denunciaRepository.save(denunciaExistente);
        return new DenunciaDTO(denunciaAtualizado);
    }

    public void deletarDenuncia(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca o Denuncia existente
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia não encontrado. ID: " + id));

        // Verifica permissões
        boolean isOwner = denuncia.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para deletar este denuncia.");
        }

        // Remove o denuncia do banco de dados
        denunciaRepository.delete(denuncia);
    }


}