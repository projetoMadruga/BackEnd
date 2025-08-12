package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.DenunciaDTO;
import Ouvidoria.Senai.dtos.DenunciaDTO;
import Ouvidoria.Senai.entities.Denuncia;
import Ouvidoria.Senai.entities.Denuncia;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.DenunciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service // A correção principal para o erro de @Autowired
public class DenunciaService {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private DenunciaRepository denunciaRepository; // Corrigido para "denunciaRepository" (camelCase)

    // Método corrigido para salvar Denúncia, não Denuncia
    public DenunciaDTO salvarDenuncia(DenunciaDTO dto, MultipartFile anexo) throws IOException {
        // 1. Pega o usuário logado do contexto de segurança
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Lógica de upload de arquivo (sem alterações)
        String caminhoAnexo = null;
        if (anexo != null && !anexo.isEmpty()) {
            String nomeArquivo = UUID.randomUUID() + "_" + anexo.getOriginalFilename();
            Path diretorio = Paths.get(UPLOAD_DIR);
            if (!Files.exists(diretorio)) {
                Files.createDirectories(diretorio);
            }
            Path caminhoArquivo = diretorio.resolve(nomeArquivo);
            anexo.transferTo(caminhoArquivo.toFile());
            caminhoAnexo = caminhoArquivo.toAbsolutePath().toString();
        }

        // 3. Cria a entidade Denuncia corretamente
        Denuncia denuncia = new Denuncia();
        denuncia.setLocal(dto.getLocal());
        denuncia.setDataHora(dto.getDataHora());
        denuncia.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        denuncia.setCaminhoAnexo(caminhoAnexo);
        denuncia.setUsuario(usuarioLogado); // Associa a denúncia ao usuário logado

        // 4. Salva no banco
        Denuncia denunciaSalva = denunciaRepository.save(denuncia);

        // 5. Retorna um DTO preenchido com os dados da denúncia salva (incluindo o ID)
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
    public DenunciaDTO atualizarDenuncia(Long id, DenunciaDTO dto, MultipartFile anexo) throws IOException {
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

        // Processa novo anexo se fornecido
        if (anexo != null && !anexo.isEmpty()) {
            // Remove o anexo anterior se existir
            if (denunciaExistente.getCaminhoAnexo() != null) {
                try {
                    Path caminhoAnterior = Paths.get(denunciaExistente.getCaminhoAnexo());
                    Files.deleteIfExists(caminhoAnterior);
                } catch (IOException e) {
                    // Log do erro, mas não interrompe o processo
                    System.err.println("Erro ao remover anexo anterior: " + e.getMessage());
                }
            }

            // Salva o novo anexo
            String novoAnexo = salvarAnexo(anexo);
            denunciaExistente.setCaminhoAnexo(novoAnexo);
        }

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

        // Remove o anexo se existir
        if (denuncia.getCaminhoAnexo() != null) {
            try {
                Path caminhoAnexo = Paths.get(denuncia.getCaminhoAnexo());
                Files.deleteIfExists(caminhoAnexo);
            } catch (IOException e) {
                // Log do erro, mas não interrompe o processo de exclusão
                System.err.println("Erro ao remover anexo: " + e.getMessage());
            }
        }

        // Remove o denuncia do banco de dados
        denunciaRepository.delete(denuncia);
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