package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.ElogioDTO;
import Ouvidoria.Senai.dtos.SugestaoDTO;
import Ouvidoria.Senai.entities.Elogio;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.Sugestao;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.SugestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
// ... outros imports

@Service
public class SugestaoService {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private SugestaoRepository sugestaoRepository;

    public SugestaoDTO salvarSugestao(SugestaoDTO dto, MultipartFile anexo) throws IOException {
        // Pega o usuário autenticado a partir do contexto de segurança
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String caminhoAnexo = null; // Lógica de upload de arquivo aqui...

        Sugestao sugestao = new Sugestao();
        sugestao.setDataHora(dto.getDataHora());
        sugestao.setLocal(dto.getLocal());
        sugestao.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        sugestao.setCaminhoAnexo(caminhoAnexo);
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
    public SugestaoDTO atualizarSugestao(Long id, SugestaoDTO dto, MultipartFile anexo) throws IOException {
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
            throw new SecurityException("Acesso negado. Você não tem permissão para atualizar este elogio.");
        }

        // Atualiza os campos
        sugestaoExistente.setDataHora(dto.getDataHora());
        sugestaoExistente.setLocal(dto.getLocal());
        sugestaoExistente.setDescricaoDetalhada(dto.getDescricaoDetalhada());

        // Processa novo anexo se fornecido
        if (anexo != null && !anexo.isEmpty()) {
            // Remove o anexo anterior se existir
            if (sugestaoExistente.getCaminhoAnexo() != null) {
                try {
                    Path caminhoAnterior = Paths.get(sugestaoExistente.getCaminhoAnexo());
                    Files.deleteIfExists(caminhoAnterior);
                } catch (IOException e) {
                    // Log do erro, mas não interrompe o processo
                    System.err.println("Erro ao remover anexo anterior: " + e.getMessage());
                }
            }

            // Salva o novo anexo
            String novoAnexo = salvarAnexo(anexo);
            sugestaoExistente.setCaminhoAnexo(novoAnexo);
        }

        // Salva as alterações
        Sugestao sugestaoAtualizada = sugestaoRepository.save(sugestaoExistente);
        return new SugestaoDTO(sugestaoAtualizada);
    }

    public void deletarSugestao(Long id) {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Busca o elogio existente
        Sugestao sugestao = sugestaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Elogio não encontrado. ID: " + id));

        // Verifica permissões
        boolean isOwner = sugestao.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new SecurityException("Acesso negado. Você não tem permissão para deletar este elogio.");
        }

        // Remove o anexo se existir
        if (sugestao.getCaminhoAnexo() != null) {
            try {
                Path caminhoAnexo = Paths.get(sugestao.getCaminhoAnexo());
                Files.deleteIfExists(caminhoAnexo);
            } catch (IOException e) {
                // Log do erro, mas não interrompe o processo de exclusão
                System.err.println("Erro ao remover anexo: " + e.getMessage());
            }
        }

        // Remove o elogio do banco de dados
        sugestaoRepository.delete(sugestao);
    }

    private String salvarAnexo(MultipartFile anexo) throws IOException {
        // Cria o diretório se não existir
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Gera um nome único para o arquivo
        String nomeOriginal = anexo.getOriginalFilename();
        String extensao = "";
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }

        String nomeArquivo = System.currentTimeMillis() + "_" + nomeOriginal;
        Path caminhoCompleto = uploadPath.resolve(nomeArquivo);

        // Salva o arquivo
        Files.copy(anexo.getInputStream(), caminhoCompleto);

        return caminhoCompleto.toString();
    }

}