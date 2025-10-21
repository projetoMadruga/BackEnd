package Ouvidoria.Senai.services;

import Ouvidoria.Senai.dtos.ManifestacaoUnificadaDTO;
import Ouvidoria.Senai.entities.*;
import Ouvidoria.Senai.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManifestacaoUnificadaService {

    @Autowired
    private ReclamacaoRepository reclamacaoRepository;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private ElogioRepository elogioRepository;

    @Autowired
    private SugestaoRepository sugestaoRepository;

    @Autowired
    private PermissaoAreaUsuarioRepository permissaoAreaUsuarioRepository;

    /**
     * Lista todas as manifestações baseado no tipo de usuário
     */
    public List<ManifestacaoUnificadaDTO> listarManifestacoes(Authentication authentication) {
        Login usuarioLogado = (Login) authentication.getPrincipal();
        
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        
        boolean isManutencao = usuarioLogado.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("MANUTENCAO"));

        List<ManifestacaoUnificadaDTO> todasManifestacoes = new ArrayList<>();

        if (isAdmin) {
            // Admin vê manifestações de acordo com suas áreas de permissão
            List<PermissaoAreaUsuario> permissoes = permissaoAreaUsuarioRepository.findByUsuario(usuarioLogado);
            
            if (permissoes.isEmpty()) {
                // Se não tem permissões específicas, vê todas
                todasManifestacoes.addAll(buscarTodasReclamacoes());
                todasManifestacoes.addAll(buscarTodasDenuncias());
                todasManifestacoes.addAll(buscarTodosElogios());
                todasManifestacoes.addAll(buscarTodasSugestoes());
            } else {
                // Filtra por áreas de permissão
                List<Area> areasPermitidas = permissoes.stream()
                        .map(PermissaoAreaUsuario::getArea)
                        .collect(Collectors.toList());
                
                todasManifestacoes.addAll(buscarManifestacoesPorAreas(areasPermitidas));
            }
        } else if (isManutencao) {
            // Manutenção vê apenas reclamações de manutenção
            List<Reclamacao> reclamacoesManutencao = reclamacaoRepository.findAllWithUsuario().stream()
                    .filter(r -> r.getTipoReclamacao() == TipoReclamacao.MANUTENCAO)
                    .collect(Collectors.toList());
            todasManifestacoes.addAll(reclamacoesManutencao.stream()
                    .map(ManifestacaoUnificadaDTO::new)
                    .collect(Collectors.toList()));
        } else {
            // Usuário comum (aluno) vê apenas suas próprias manifestações
            todasManifestacoes.addAll(buscarReclamacoesDoUsuario(usuarioLogado));
            todasManifestacoes.addAll(buscarDenunciasDoUsuario(usuarioLogado));
            todasManifestacoes.addAll(buscarElogiosDoUsuario(usuarioLogado));
            todasManifestacoes.addAll(buscarSugestoesDoUsuario(usuarioLogado));
        }

        return todasManifestacoes;
    }

    /**
     * Lista manifestações por tipo (apenas para admins)
     */
    public List<ManifestacaoUnificadaDTO> listarManifestacoesPorTipo(String tipo) {
        switch (tipo.toUpperCase()) {
            case "RECLAMACAO":
                return buscarTodasReclamacoes();
            case "DENUNCIA":
                return buscarTodasDenuncias();
            case "ELOGIO":
                return buscarTodosElogios();
            case "SUGESTAO":
                return buscarTodasSugestoes();
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Busca uma manifestação específica por ID
     */
    public ManifestacaoUnificadaDTO buscarPorId(Long id, Authentication authentication) {
        Login usuarioLogado = (Login) authentication.getPrincipal();
        
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

        // Tenta buscar em cada tipo de manifestação
        // Reclamações
        try {
            Reclamacao reclamacao = reclamacaoRepository.findById(id).orElse(null);
            if (reclamacao != null) {
                if (isAdmin || reclamacao.getUsuario().getId().equals(usuarioLogado.getId())) {
                    return new ManifestacaoUnificadaDTO(reclamacao);
                }
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Denúncias
        try {
            Denuncia denuncia = denunciaRepository.findById(id).orElse(null);
            if (denuncia != null) {
                if (isAdmin || denuncia.getUsuario().getId().equals(usuarioLogado.getId())) {
                    return new ManifestacaoUnificadaDTO(denuncia);
                }
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Elogios
        try {
            Elogio elogio = elogioRepository.findById(id).orElse(null);
            if (elogio != null) {
                if (isAdmin || elogio.getUsuario().getId().equals(usuarioLogado.getId())) {
                    return new ManifestacaoUnificadaDTO(elogio);
                }
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Sugestões
        try {
            Sugestao sugestao = sugestaoRepository.findById(id).orElse(null);
            if (sugestao != null) {
                if (isAdmin || sugestao.getUsuario().getId().equals(usuarioLogado.getId())) {
                    return new ManifestacaoUnificadaDTO(sugestao);
                }
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        throw new RuntimeException("Manifestação não encontrada ou acesso negado");
    }

    // Métodos auxiliares para buscar manifestações
    private List<ManifestacaoUnificadaDTO> buscarTodasReclamacoes() {
        return reclamacaoRepository.findAllWithUsuario().stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    private List<ManifestacaoUnificadaDTO> buscarTodasDenuncias() {
        return denunciaRepository.findAllWithUsuario().stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    private List<ManifestacaoUnificadaDTO> buscarTodosElogios() {
        return elogioRepository.findAllWithUsuario().stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    private List<ManifestacaoUnificadaDTO> buscarTodasSugestoes() {
        return sugestaoRepository.findAllWithUsuario().stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    private List<ManifestacaoUnificadaDTO> buscarReclamacoesDoUsuario(Login usuario) {
        return reclamacaoRepository.findByUsuarioWithDetails(usuario).stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    private List<ManifestacaoUnificadaDTO> buscarDenunciasDoUsuario(Login usuario) {
        return denunciaRepository.findByUsuarioWithDetails(usuario).stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    private List<ManifestacaoUnificadaDTO> buscarElogiosDoUsuario(Login usuario) {
        return elogioRepository.findByUsuarioWithDetails(usuario).stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    private List<ManifestacaoUnificadaDTO> buscarSugestoesDoUsuario(Login usuario) {
        return sugestaoRepository.findByUsuarioWithDetails(usuario).stream()
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca manifestações filtradas por áreas permitidas
     */
    private List<ManifestacaoUnificadaDTO> buscarManifestacoesPorAreas(List<Area> areasPermitidas) {
        List<ManifestacaoUnificadaDTO> manifestacoes = new ArrayList<>();
        
        // Busca todas as manifestações
        List<Reclamacao> reclamacoes = reclamacaoRepository.findAllWithUsuario();
        List<Denuncia> denuncias = denunciaRepository.findAllWithUsuario();
        List<Elogio> elogios = elogioRepository.findAllWithUsuario();
        List<Sugestao> sugestoes = sugestaoRepository.findAllWithUsuario();
        
        // Filtra por área
        manifestacoes.addAll(reclamacoes.stream()
                .filter(r -> r.getArea() == null || areasPermitidas.contains(r.getArea()))
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList()));
        
        manifestacoes.addAll(denuncias.stream()
                .filter(d -> d.getArea() == null || areasPermitidas.contains(d.getArea()))
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList()));
        
        manifestacoes.addAll(elogios.stream()
                .filter(e -> e.getArea() == null || areasPermitidas.contains(e.getArea()))
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList()));
        
        manifestacoes.addAll(sugestoes.stream()
                .filter(s -> s.getArea() == null || areasPermitidas.contains(s.getArea()))
                .map(ManifestacaoUnificadaDTO::new)
                .collect(Collectors.toList()));
        
        return manifestacoes;
    }

    /**
     * Atualiza uma manifestação (apenas para admins)
     */
    public ManifestacaoUnificadaDTO atualizarManifestacao(Long id, ManifestacaoUnificadaDTO dto, Authentication authentication) {
        Login usuarioLogado = (Login) authentication.getPrincipal();
        
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            throw new SecurityException("Acesso negado. Apenas administradores podem atualizar manifestações.");
        }
        
        // Verifica permissões de área do admin
        List<PermissaoAreaUsuario> permissoes = permissaoAreaUsuarioRepository.findByUsuario(usuarioLogado);
        List<Area> areasPermitidas = permissoes.stream()
                .map(PermissaoAreaUsuario::getArea)
                .collect(Collectors.toList());

        // Tenta atualizar em cada tipo de manifestação
        // Reclamações
        try {
            Reclamacao reclamacao = reclamacaoRepository.findById(id).orElse(null);
            if (reclamacao != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && reclamacao.getArea() != null && !areasPermitidas.contains(reclamacao.getArea())) {
                    throw new SecurityException("Você não tem permissão para editar manifestações desta área.");
                }
                
                reclamacao.setLocal(dto.getLocal());
                reclamacao.setDataHora(dto.getDataHora());
                reclamacao.setDescricaoDetalhada(dto.getDescricaoDetalhada());
                reclamacao.setObservacao(dto.getObservacao());
                if (dto.getStatus() != null) {
                    reclamacao.setStatus(StatusReclamacao.valueOf(dto.getStatus()));
                }
                reclamacaoRepository.save(reclamacao);
                return new ManifestacaoUnificadaDTO(reclamacao);
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Denúncias
        try {
            Denuncia denuncia = denunciaRepository.findById(id).orElse(null);
            if (denuncia != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && denuncia.getArea() != null && !areasPermitidas.contains(denuncia.getArea())) {
                    throw new SecurityException("Você não tem permissão para editar manifestações desta área.");
                }
                
                denuncia.setLocal(dto.getLocal());
                denuncia.setDataHora(dto.getDataHora());
                denuncia.setDescricaoDetalhada(dto.getDescricaoDetalhada());
                denuncia.setObservacao(dto.getObservacao());
                if (dto.getStatus() != null) {
                    denuncia.setStatus(StatusDenuncia.valueOf(dto.getStatus()));
                }
                denunciaRepository.save(denuncia);
                return new ManifestacaoUnificadaDTO(denuncia);
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Elogios
        try {
            Elogio elogio = elogioRepository.findById(id).orElse(null);
            if (elogio != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && elogio.getArea() != null && !areasPermitidas.contains(elogio.getArea())) {
                    throw new SecurityException("Você não tem permissão para editar manifestações desta área.");
                }
                
                elogio.setLocal(dto.getLocal());
                elogio.setDataHora(dto.getDataHora());
                elogio.setDescricaoDetalhada(dto.getDescricaoDetalhada());
                elogio.setObservacao(dto.getObservacao());
                if (dto.getStatus() != null) {
                    elogio.setStatus(StatusElogio.valueOf(dto.getStatus()));
                }
                elogioRepository.save(elogio);
                return new ManifestacaoUnificadaDTO(elogio);
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Sugestões
        try {
            Sugestao sugestao = sugestaoRepository.findById(id).orElse(null);
            if (sugestao != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && sugestao.getArea() != null && !areasPermitidas.contains(sugestao.getArea())) {
                    throw new SecurityException("Você não tem permissão para editar manifestações desta área.");
                }
                
                sugestao.setLocal(dto.getLocal());
                sugestao.setDataHora(dto.getDataHora());
                sugestao.setDescricaoDetalhada(dto.getDescricaoDetalhada());
                sugestao.setObservacao(dto.getObservacao());
                if (dto.getStatus() != null) {
                    sugestao.setStatus(StatusSugestao.valueOf(dto.getStatus()));
                }
                sugestaoRepository.save(sugestao);
                return new ManifestacaoUnificadaDTO(sugestao);
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        throw new RuntimeException("Manifestação não encontrada");
    }

    /**
     * Deleta uma manifestação (apenas para admins)
     */
    public void deletarManifestacao(Long id, Authentication authentication) {
        Login usuarioLogado = (Login) authentication.getPrincipal();
        
        boolean isAdmin = usuarioLogado.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            throw new SecurityException("Acesso negado. Apenas administradores podem deletar manifestações.");
        }
        
        // Verifica permissões de área do admin
        List<PermissaoAreaUsuario> permissoes = permissaoAreaUsuarioRepository.findByUsuario(usuarioLogado);
        List<Area> areasPermitidas = permissoes.stream()
                .map(PermissaoAreaUsuario::getArea)
                .collect(Collectors.toList());

        // Tenta deletar em cada tipo de manifestação
        // Reclamações
        try {
            Reclamacao reclamacao = reclamacaoRepository.findById(id).orElse(null);
            if (reclamacao != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && reclamacao.getArea() != null && !areasPermitidas.contains(reclamacao.getArea())) {
                    throw new SecurityException("Você não tem permissão para deletar manifestações desta área.");
                }
                
                reclamacaoRepository.delete(reclamacao);
                return;
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Denúncias
        try {
            Denuncia denuncia = denunciaRepository.findById(id).orElse(null);
            if (denuncia != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && denuncia.getArea() != null && !areasPermitidas.contains(denuncia.getArea())) {
                    throw new SecurityException("Você não tem permissão para deletar manifestações desta área.");
                }
                
                denunciaRepository.delete(denuncia);
                return;
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Elogios
        try {
            Elogio elogio = elogioRepository.findById(id).orElse(null);
            if (elogio != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && elogio.getArea() != null && !areasPermitidas.contains(elogio.getArea())) {
                    throw new SecurityException("Você não tem permissão para deletar manifestações desta área.");
                }
                
                elogioRepository.delete(elogio);
                return;
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        // Sugestões
        try {
            Sugestao sugestao = sugestaoRepository.findById(id).orElse(null);
            if (sugestao != null) {
                // Valida permissão de área
                if (!areasPermitidas.isEmpty() && sugestao.getArea() != null && !areasPermitidas.contains(sugestao.getArea())) {
                    throw new SecurityException("Você não tem permissão para deletar manifestações desta área.");
                }
                
                sugestaoRepository.delete(sugestao);
                return;
            }
        } catch (Exception e) {
            // Continua tentando outros tipos
        }

        throw new RuntimeException("Manifestação não encontrada");
    }
}
