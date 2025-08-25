package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.SugestaoDTO;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.Sugestao;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.SugestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SugestaoService {

    @Autowired
    private SugestaoRepository sugestaoRepository;

    public SugestaoDTO salvarSugestao(SugestaoDTO dto) {
        // Pega o usuário autenticado a partir do contexto de segurança
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Sugestao sugestao = new Sugestao();
        sugestao.setDataHora(dto.getDataHora());
        sugestao.setLocal(dto.getLocal());
        sugestao.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        sugestao.setUsuario(usuarioLogado); // Associa o usuário logado

        sugestao = sugestaoRepository.save(sugestao);
        return new SugestaoDTO(sugestao);
    }

    public SugestaoDTO buscarPorId(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Usa a consulta otimizada com JOIN FETCH
        Sugestao sugestao = sugestaoRepository.findByIdWithUsuario(id);
        if (sugestao == null) {
            throw new ResourceNotFoundException("Sugestão não encontrada. ID: " + id);
        }

        // REGRA: Ou você é o dono da sugestão, ou você é ADMIN.
        boolean isOwner = sugestao.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isOwner || isAdmin) {
            return new SugestaoDTO(sugestao);
        } else {
            // Lança uma exceção de acesso negado se a regra não for cumprida.
            throw new SecurityException("Acesso negado.");
        }
    }

    public List<SugestaoDTO> listarManifestacoes() {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Sugestao> sugestoes;

        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (isAdmin) {
            // Se for ADMIN, busca todas as sugestões com JOIN FETCH
            sugestoes = sugestaoRepository.findAllWithUsuario();
        } else {
            // Se não for ADMIN, busca apenas as do próprio usuário com JOIN FETCH
            sugestoes = sugestaoRepository.findByUsuarioWithDetails(usuarioLogado);
        }

        return sugestoes.stream().map(SugestaoDTO::new).collect(Collectors.toList());
    }
    public SugestaoDTO atualizarSugestao(Long id, SugestaoDTO dto) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca a sugestão existente usando JOIN FETCH
        Sugestao sugestaoExistente = sugestaoRepository.findByIdWithUsuario(id);
        if (sugestaoExistente == null) {
            throw new ResourceNotFoundException("Sugestão não encontrada. ID: " + id);
        }

        // Verifica permissões
        boolean isOwner = sugestaoExistente.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para atualizar esta sugestão.");
        }

        // Atualiza os campos
        sugestaoExistente.setDataHora(dto.getDataHora());
        sugestaoExistente.setLocal(dto.getLocal());
        sugestaoExistente.setDescricaoDetalhada(dto.getDescricaoDetalhada());

        // Salva as alterações
        Sugestao sugestaoAtualizada = sugestaoRepository.save(sugestaoExistente);
        return new SugestaoDTO(sugestaoAtualizada);
    }

    public void deletarSugestao(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca a sugestão existente
        Sugestao sugestao = sugestaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sugestão não encontrada. ID: " + id));

        // Verifica permissões
        boolean isOwner = sugestao.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para deletar esta sugestão.");
        }

        // Remove a sugestão do banco de dados
        sugestaoRepository.delete(sugestao);
    }



}