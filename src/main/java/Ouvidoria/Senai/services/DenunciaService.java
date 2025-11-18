package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.DenunciaDTO;
import Ouvidoria.Senai.entities.Denuncia;
import Ouvidoria.Senai.entities.Login;
import Ouvidoria.Senai.entities.Area;
import Ouvidoria.Senai.exceptions.ResourceNotFoundException;
import Ouvidoria.Senai.repositories.DenunciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DenunciaService {

    private static final Logger logger = LoggerFactory.getLogger(DenunciaService.class);

    @Autowired
    private DenunciaRepository denunciaRepository;

    public DenunciaDTO salvarDenuncia(DenunciaDTO dto) {
        // Pega o usuário logado do contexto de segurança
        Login usuarioLogado = (Login) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // DEBUG LOGS - MUITO VISÍVEIS
        logger.warn("╔════════════════════════════════════════╗");
        logger.warn("║  DENUNCIA SERVICE INICIADO - RECEBIDO  ║");
        logger.warn("╚════════════════════════════════════════╝");
        logger.warn("DTO.area RECEBIDO = [" + dto.getArea() + "]");
        logger.warn("DTO.area == null? " + (dto.getArea() == null));
        logger.warn("DTO.area.isBlank()? " + (dto.getArea() != null ? dto.getArea().isBlank() : "N/A"));
        logger.warn("DTO.toString(): " + dto.toString());
        logger.warn("╚════════════════════════════════════════╝");

        // Loga o conteúdo recebido no DTO, incluindo a área
        logger.info("SalvarDenuncia - DTO recebido: local={}, dataHora={}, area={}", 
                dto.getLocal(), dto.getDataHora(), dto.getArea());

        // Cria a entidade Denuncia
        Denuncia denuncia = new Denuncia();
        denuncia.setLocal(dto.getLocal());
        denuncia.setDataHora(dto.getDataHora());
        denuncia.setDescricaoDetalhada(dto.getDescricaoDetalhada());
        denuncia.setUsuario(usuarioLogado); // Associa a denúncia ao usuário logado

        // Converte a String enviada pelo front-end para o enum Area, com fallback
        String areaStr = dto.getArea();
        logger.warn(">>> INICIANDO CONVERSÃO DE AREA <<<");
        logger.warn("areaStr = [" + areaStr + "]");
        logger.warn("areaStr == null? " + (areaStr == null));
        logger.warn("areaStr.isBlank()? " + (areaStr != null ? areaStr.isBlank() : "N/A"));
        
        if (areaStr != null && !areaStr.isBlank()) {
            try {
                logger.warn(">>> TENTANDO CONVERTER: " + areaStr);
                Area areaEnum = Area.valueOf(areaStr);
                denuncia.setArea(areaEnum);
                logger.info("SalvarDenuncia - Área convertida para enum com sucesso: {}", areaStr);
                logger.warn("✓✓✓ SUCESSO! Área convertida: " + areaEnum);
            } catch (IllegalArgumentException ex) {
                logger.error("SalvarDenuncia - Erro ao converter área: {} - {}", areaStr, ex.getMessage());
                logger.error("✗✗✗ ERRO ao converter área em DenunciaService: " + areaStr);
                logger.error("✗✗✗ Mensagem: " + ex.getMessage());
                logger.error("✗✗✗ Aplicando fallback FACULDADE_SENAI");
                denuncia.setArea(Area.FACULDADE_SENAI);
            }
        } else {
            logger.info("SalvarDenuncia - Área no DTO é nula ou vazia, aplicando fallback FACULDADE_SENAI");
            logger.error("✗✗✗ AVISO: Área recebida como null/vazia em DenunciaService");
            logger.error("✗✗✗ DTO.toString(): " + dto.toString());
            logger.error("✗✗✗ Aplicando fallback FACULDADE_SENAI");
            denuncia.setArea(Area.FACULDADE_SENAI);
        }
        logger.warn(">>> FIM DA CONVERSÃO - Area final: " + denuncia.getArea());

        // Salva no banco
        Denuncia denunciaSalva = denunciaRepository.save(denuncia);

        // DEBUG: Verifica o que foi salvo
        logger.warn("╔════════════════════════════════════════╗");
        logger.warn("║   DENUNCIA SALVA NO BANCO - DEBUG      ║");
        logger.warn("╚════════════════════════════════════════╝");
        logger.warn("Denuncia.area após salvar = [" + denunciaSalva.getArea() + "]");
        logger.warn("Denuncia.id = [" + denunciaSalva.getId() + "]");
        logger.warn("╚════════════════════════════════════════╝");

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