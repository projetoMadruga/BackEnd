package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.ReclamacaoDTO;
import Ouvidoria.Senai.dtos.ReclamacaoDTO;
import Ouvidoria.Senai.entities.Reclamacao;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.Reclamacao;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException; // 1. Importar nossa exceção personalizada
import Ouvidoria.Senai.repositories.ReclamacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReclamacaoService {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private ReclamacaoRepository reclamacaoRepository;

    // 2. MÉTODO RENOMEADO CORRETAMENTE
    public ReclamacaoDTO salvarReclamacao(ReclamacaoDTO dto, MultipartFile anexo) throws IOException {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String caminhoAnexo = null;
        if (anexo != null && !anexo.isEmpty()) {
            String nomeArquivo = UUID.randomUUID() + "_" + anexo.getOriginalFilename();
            Path diretorio = Paths.get("uploads/");
            if (!Files.exists(diretorio)) {
                Files.createDirectories(diretorio);
            }
            Path caminhoArquivo = diretorio.resolve(nomeArquivo);
            anexo.transferTo(caminhoArquivo.toFile());
            caminhoAnexo = caminhoArquivo.toAbsolutePath().toString();
        }

        Reclamacao reclamacao = new Reclamacao();
        reclamacao.setDataHora(dto.getDataHora());
        reclamacao.setLocal(dto.getLocal());
        reclamacao.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        reclamacao.setCaminhoAnexo(caminhoAnexo);
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

    public ReclamacaoDTO atualizarReclamacao(Long id, ReclamacaoDTO dto, MultipartFile anexo) throws IOException {
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

        // Processa novo anexo se fornecido
        if (anexo != null && !anexo.isEmpty()) {
            // Remove o anexo anterior se existir
            if (reclamacaoExistente.getCaminhoAnexo() != null) {
                try {
                    Path caminhoAnterior = Paths.get(reclamacaoExistente.getCaminhoAnexo());
                    Files.deleteIfExists(caminhoAnterior);
                } catch (IOException e) {
                    // Log do erro, mas não interrompe o processo
                    System.err.println("Erro ao remover anexo anterior: " + e.getMessage());
                }
            }

            // Salva o novo anexo
            String novoAnexo = salvarAnexo(anexo);
            reclamacaoExistente.setCaminhoAnexo(novoAnexo);
        }

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

        // Remove o anexo se existir
        if (Reclamacao.getCaminhoAnexo() != null) {
            try {
                Path caminhoAnexo = Paths.get(Reclamacao.getCaminhoAnexo());
                Files.deleteIfExists(caminhoAnexo);
            } catch (IOException e) {
                // Log do erro, mas não interrompe o processo de exclusão
                System.err.println("Erro ao remover anexo: " + e.getMessage());
            }
        }

        // Remove o Reclamacao do banco de dados
        reclamacaoRepository.delete(Reclamacao);
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
