package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.ReclamacaoDTO;
import Ouvidoria.Senai.entities.Reclamacao;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.StatusReclamacao;
import Ouvidoria.Senai.entities.TipoReclamacao;
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
        reclamacao.setStatus(StatusReclamacao.PENDENTE); // Status inicial é sempre pendente
        reclamacao.setTipoReclamacao(dto.getTipoReclamacao()); // Define o tipo de reclamação

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
        boolean isManutencao = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("MANUTENCAO"));

        if (isAdmin) {
            // Admin vê todas as reclamações
            reclamacoes = reclamacaoRepository.findAllWithUsuario();
        } else if (isManutencao) {
            // Manutenção vê apenas reclamações do tipo MANUTENCAO
            reclamacoes = reclamacaoRepository.findAllWithUsuario().stream()
                .filter(r -> r.getTipoReclamacao() == TipoReclamacao.MANUTENCAO)
                .collect(Collectors.toList());
        } else {
            // Usuários comuns veem apenas suas próprias reclamações
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
        boolean isManutencao = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("MANUTENCAO"));

        // Verifica se é uma reclamação de manutenção e o usuário é da manutenção
        boolean isManutencaoReclamacao = reclamacaoExistente.getTipoReclamacao() == TipoReclamacao.MANUTENCAO && isManutencao;

        if (!isOwner && !isAdmin && !isManutencaoReclamacao) {
            throw new SecurityException("Acesso negado. Você não tem permissão para atualizar esta Reclamação.");
        }

        // Se for o proprietário ou admin, pode atualizar todos os campos
        if (isOwner || isAdmin) {
            reclamacaoExistente.setDataHora(dto.getDataHora());
            reclamacaoExistente.setLocal(dto.getLocal());
            reclamacaoExistente.setDescricaoDetalhada(dto.getDescricaoDetalhada());
            reclamacaoExistente.setTipoReclamacao(dto.getTipoReclamacao());
        }
        
        // Se for admin ou manutenção, pode atualizar status e observação
        if (isAdmin || isManutencaoReclamacao) {
            reclamacaoExistente.setStatus(dto.getStatus());
            reclamacaoExistente.setObservacao(dto.getObservacao());
        }

        // Salva as alterações
        Reclamacao reclamacaoAtualizada = reclamacaoRepository.save(reclamacaoExistente);
        return new ReclamacaoDTO(reclamacaoAtualizada);
    }

    public void deletarReclamacao(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca a reclamação existente
        Reclamacao reclamacaoExistente = reclamacaoRepository.findByIdWithUsuario(id);
        if (reclamacaoExistente == null) {
            throw new ResourceNotFoundException("Reclamação não encontrada. ID: " + id);
        }

        // Verifica permissões
        boolean isOwner = reclamacaoExistente.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));
        boolean isManutencao = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("MANUTENCAO"));
        boolean isManutencaoReclamacao = reclamacaoExistente.getTipoReclamacao() == TipoReclamacao.MANUTENCAO && isManutencao;

        if (!isOwner && !isAdmin && !isManutencaoReclamacao) {
            throw new SecurityException("Acesso negado. Você não tem permissão para deletar esta reclamação.");
        }

        // Deleta a reclamação
        reclamacaoRepository.delete(reclamacaoExistente);
    }


}
