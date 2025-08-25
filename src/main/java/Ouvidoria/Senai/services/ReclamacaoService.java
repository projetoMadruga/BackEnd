package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.ReclamacaoDTO;
import Ouvidoria.Senai.entities.Reclamacao;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.ReclamacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReclamacaoService {

    @Autowired
    private ReclamacaoRepository reclamacaoRepository;

    public ReclamacaoDTO salvarReclamacao(ReclamacaoDTO dto) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Reclamacao reclamacao = new Reclamacao();
        reclamacao.setDataHora(dto.getDataHora());
        reclamacao.setLocal(dto.getLocal());
        reclamacao.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        reclamacao.setUsuario(usuarioLogado);

        reclamacao = reclamacaoRepository.save(reclamacao);
        return new ReclamacaoDTO(reclamacao);
    }

    public ReclamacaoDTO buscarPorId(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Usa a consulta otimizada com JOIN FETCH
        Reclamacao reclamacao = reclamacaoRepository.findByIdWithUsuario(id);
        if (reclamacao == null) {
            throw new ResourceNotFoundException("Reclamação não encontrada. ID: " + id);
        }

        boolean isOwner = reclamacao.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isOwner || isAdmin) {
            return new ReclamacaoDTO(reclamacao);
        } else {
            throw new SecurityException("Acesso negado.");
        }
    }

    public List<ReclamacaoDTO> listarManifestacoes() {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Reclamacao> reclamacoes;

        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            // Usa a consulta otimizada com JOIN FETCH para todas as reclamações
            reclamacoes = reclamacaoRepository.findAllWithUsuario();
        } else {
            // Usa a consulta otimizada com JOIN FETCH para reclamações do usuário
            reclamacoes = reclamacaoRepository.findByUsuarioWithDetails(usuarioLogado);
        }

        return reclamacoes.stream().map(ReclamacaoDTO::new).collect(Collectors.toList());
    }

    public ReclamacaoDTO atualizarReclamacao(Long id, ReclamacaoDTO dto) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca a reclamação existente usando JOIN FETCH
        Reclamacao reclamacaoExistente = reclamacaoRepository.findByIdWithUsuario(id);
        if (reclamacaoExistente == null) {
            throw new ResourceNotFoundException("Reclamação não encontrada. ID: " + id);
        }

        // Verifica permissões
        boolean isOwner = reclamacaoExistente.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para atualizar este Reclamacao.");
        }

        // Atualiza os campos
        reclamacaoExistente.setDataHora(dto.getDataHora());
        reclamacaoExistente.setLocal(dto.getLocal());
        reclamacaoExistente.setDescricaoDetalhada(dto.getDescricaoDetalhada());

        // Salva as alterações
        Reclamacao ReclamacaoAtualizado = reclamacaoRepository.save(reclamacaoExistente);
        return new ReclamacaoDTO(ReclamacaoAtualizado);
    }

    public void deletarReclamacao(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca o Reclamacao existente
        Reclamacao Reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reclamacao não encontrado. ID: " + id));

        // Verifica permissões
        boolean isOwner = Reclamacao.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para deletar este Reclamacao.");
        }

        // Remove o Reclamacao do banco de dados
        reclamacaoRepository.delete(Reclamacao);
    }


}
