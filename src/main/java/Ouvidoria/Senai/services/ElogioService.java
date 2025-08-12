package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.ElogioDTO;
import Ouvidoria.Senai.entities.Elogio;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.ElogioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElogioService {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private ElogioRepository elogioRepository;

    public ElogioDTO salvarElogio(ElogioDTO dto, MultipartFile anexo) throws IOException {
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String caminhoAnexo = null;
        if (anexo != null && !anexo.isEmpty()) {
            // ... lógica de upload de arquivo ...
        }

        Elogio elogio = new Elogio(); // Usa o construtor padrão
        elogio.setDataHora(dto.getDataHora());
        elogio.setLocal(dto.getLocal());
        elogio.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        elogio.setCaminhoAnexo(caminhoAnexo);
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

    public ElogioDTO atualizarElogio(Long id, ElogioDTO dto, MultipartFile anexo) throws IOException {
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

        // Processa novo anexo se fornecido
        if (anexo != null && !anexo.isEmpty()) {
            // Remove o anexo anterior se existir
            if (elogioExistente.getCaminhoAnexo() != null) {
                try {
                    Path caminhoAnterior = Paths.get(elogioExistente.getCaminhoAnexo());
                    Files.deleteIfExists(caminhoAnterior);
                } catch (IOException e) {
                    // Log do erro, mas não interrompe o processo
                    System.err.println("Erro ao remover anexo anterior: " + e.getMessage());
                }
            }

            // Salva o novo anexo
            String novoAnexo = salvarAnexo(anexo);
            elogioExistente.setCaminhoAnexo(novoAnexo);
        }

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

        // Remove o anexo se existir
        if (elogio.getCaminhoAnexo() != null) {
            try {
                Path caminhoAnexo = Paths.get(elogio.getCaminhoAnexo());
                Files.deleteIfExists(caminhoAnexo);
            } catch (IOException e) {
                // Log do erro, mas não interrompe o processo de exclusão
                System.err.println("Erro ao remover anexo: " + e.getMessage());
            }
        }

        // Remove o elogio do banco de dados
        elogioRepository.delete(elogio);
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