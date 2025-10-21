package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.ElogioDTO;
import Ouvidoria.Senai.entities.Elogio;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.ElogioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElogioService {

    @Autowired
    private ElogioRepository elogioRepository;

    public ElogioDTO salvarElogio(ElogioDTO dto) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Elogio elogio = new Elogio(); // Usa o construtor padrão
        elogio.setDataHora(dto.getDataHora());
        elogio.setLocal(dto.getLocal());
        elogio.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        elogio.setUsuario(usuarioLogado); // Associa o usuário logado

        Elogio elogioSalvo = elogioRepository.save(elogio);
        return new ElogioDTO(elogioSalvo); // Retorna um DTO preenchido
    }

    public ElogioDTO buscarPorId(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Usa a consulta otimizada com JOIN FETCH
        Elogio elogio = elogioRepository.findByIdWithUsuario(id);
        if (elogio == null) {
            throw new ResourceNotFoundException("Elogio não encontrado. ID: " + id);
        }

        boolean isOwner = elogio.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isOwner || isAdmin) {
            return new ElogioDTO(elogio);
        } else {
            throw new SecurityException("Acesso negado.");
        }
    }

    public List<ElogioDTO> listarManifestacoes() {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Elogio> elogios;
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            // Usa a consulta otimizada com JOIN FETCH para todos os elogios
            elogios = elogioRepository.findAllWithUsuario();
        } else {
            // Usa a consulta otimizada com JOIN FETCH para elogios do usuário
            elogios = elogioRepository.findByUsuarioWithDetails(usuarioLogado);
        }

        return elogios.stream().map(ElogioDTO::new).collect(Collectors.toList());
    }

    public ElogioDTO atualizarElogio(Long id, ElogioDTO dto) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca o elogio existente usando JOIN FETCH
        Elogio elogioExistente = elogioRepository.findByIdWithUsuario(id);
        if (elogioExistente == null) {
            throw new ResourceNotFoundException("Elogio não encontrado. ID: " + id);
        }

        // Verifica permissões
        boolean isOwner = elogioExistente.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para atualizar este elogio.");
        }

        // Atualiza os campos
        elogioExistente.setDataHora(dto.getDataHora());
        elogioExistente.setLocal(dto.getLocal());
        elogioExistente.setDescricaoDetalhada(dto.getDescricaoDetalhada());

        // Salva as alterações
        Elogio elogioAtualizado = elogioRepository.save(elogioExistente);
        return new ElogioDTO(elogioAtualizado);
    }

    public void deletarElogio(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca o elogio existente usando JOIN FETCH
        Elogio elogio = elogioRepository.findByIdWithUsuario(id);
        if (elogio == null) {
            throw new ResourceNotFoundException("Elogio não encontrado. ID: " + id);
        }

        // Verifica permissões
        boolean isOwner = elogio.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para deletar este elogio.");
        }

        // Remove o elogio do banco de dados
        elogioRepository.delete(elogio);
    }


}