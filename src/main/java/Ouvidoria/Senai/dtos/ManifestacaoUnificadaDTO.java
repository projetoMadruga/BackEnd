package Ouvidoria.Senai.dtos;

import Ouvidoria.Senai.entities.*;

public class ManifestacaoUnificadaDTO {
    private Long id;
    private String tipo; // "RECLAMACAO", "DENUNCIA", "ELOGIO", "SUGESTAO"
    private String local;
    private String dataHora;
    private String descricaoDetalhada;
    private String caminhoAnexo;
    private String status;
    private String observacao;
    private String tipoReclamacao; // Para reclamações
    private String emailUsuario;
    private String nomeUsuario;
    private String cargoUsuario;
    private String area;

    public ManifestacaoUnificadaDTO() {}

    // Construtor para Reclamação
    public ManifestacaoUnificadaDTO(Reclamacao reclamacao) {
        this.id = reclamacao.getId();
        this.tipo = "RECLAMACAO";
        this.local = reclamacao.getLocal();
        this.dataHora = reclamacao.getDataHora();
        this.descricaoDetalhada = reclamacao.getDescricaoDetalhada();
        this.caminhoAnexo = reclamacao.getCaminhoAnexo();
        this.status = reclamacao.getStatus() != null ? reclamacao.getStatus().toString() : "PENDENTE";
        this.observacao = reclamacao.getObservacao();
        this.tipoReclamacao = reclamacao.getTipoReclamacao() != null ? reclamacao.getTipoReclamacao().toString() : null;
        this.emailUsuario = reclamacao.getUsuario().getEmailEducacional();
        this.nomeUsuario = reclamacao.getUsuario().getEmailEducacional();
        this.cargoUsuario = reclamacao.getUsuario().getCargoUsuario().toString();
        this.area = reclamacao.getArea() != null ? reclamacao.getArea().toString() : null;
    }

    // Construtor para Denúncia
    public ManifestacaoUnificadaDTO(Denuncia denuncia) {
        this.id = denuncia.getId();
        this.tipo = "DENUNCIA";
        this.local = denuncia.getLocal();
        this.dataHora = denuncia.getDataHora();
        this.descricaoDetalhada = denuncia.getDescricaoDetalhada();
        this.caminhoAnexo = denuncia.getCaminhoAnexo();
        this.status = denuncia.getStatus() != null ? denuncia.getStatus().toString() : "PENDENTE";
        this.observacao = denuncia.getObservacao();
        this.emailUsuario = denuncia.getUsuario().getEmailEducacional();
        this.nomeUsuario = denuncia.getUsuario().getEmailEducacional();
        this.cargoUsuario = denuncia.getUsuario().getCargoUsuario().toString();
        this.area = denuncia.getArea() != null ? denuncia.getArea().toString() : null;
    }

    // Construtor para Elogio
    public ManifestacaoUnificadaDTO(Elogio elogio) {
        this.id = elogio.getId();
        this.tipo = "ELOGIO";
        this.local = elogio.getLocal();
        this.dataHora = elogio.getDataHora();
        this.descricaoDetalhada = elogio.getDescricaoDetalhada();
        this.caminhoAnexo = elogio.getCaminhoAnexo();
        this.status = elogio.getStatus() != null ? elogio.getStatus().toString() : "PENDENTE";
        this.observacao = elogio.getObservacao();
        this.emailUsuario = elogio.getUsuario().getEmailEducacional();
        this.nomeUsuario = elogio.getUsuario().getEmailEducacional();
        this.cargoUsuario = elogio.getUsuario().getCargoUsuario().toString();
        this.area = elogio.getArea() != null ? elogio.getArea().toString() : null;
    }

    // Construtor para Sugestão
    public ManifestacaoUnificadaDTO(Sugestao sugestao) {
        this.id = sugestao.getId();
        this.tipo = "SUGESTAO";
        this.local = sugestao.getLocal();
        this.dataHora = sugestao.getDataHora();
        this.descricaoDetalhada = sugestao.getDescricaoDetalhada();
        this.caminhoAnexo = sugestao.getCaminhoAnexo();
        this.status = sugestao.getStatus() != null ? sugestao.getStatus().toString() : "PENDENTE";
        this.observacao = sugestao.getObservacao();
        this.emailUsuario = sugestao.getUsuario().getEmailEducacional();
        this.nomeUsuario = sugestao.getUsuario().getEmailEducacional();
        this.cargoUsuario = sugestao.getUsuario().getCargoUsuario().toString();
        this.area = sugestao.getArea() != null ? sugestao.getArea().toString() : null;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public String getDescricaoDetalhada() { return descricaoDetalhada; }
    public void setDescricaoDetalhada(String descricaoDetalhada) { this.descricaoDetalhada = descricaoDetalhada; }

    public String getCaminhoAnexo() { return caminhoAnexo; }
    public void setCaminhoAnexo(String caminhoAnexo) { this.caminhoAnexo = caminhoAnexo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getTipoReclamacao() { return tipoReclamacao; }
    public void setTipoReclamacao(String tipoReclamacao) { this.tipoReclamacao = tipoReclamacao; }

    public String getEmailUsuario() { return emailUsuario; }
    public void setEmailUsuario(String emailUsuario) { this.emailUsuario = emailUsuario; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getCargoUsuario() { return cargoUsuario; }
    public void setCargoUsuario(String cargoUsuario) { this.cargoUsuario = cargoUsuario; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}
