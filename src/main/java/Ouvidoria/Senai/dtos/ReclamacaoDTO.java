package Ouvidoria.Senai.dtos;

import Ouvidoria.Senai.entities.Reclamacao;
import jakarta.persistence.Lob;

public class ReclamacaoDTO {

    private Long id;
    private String dataHora;
    private String Usuario;
    private String local;
    private String descricaoDetalhada;

    private String caminhoAnexo;

    public ReclamacaoDTO() {

    }

    public ReclamacaoDTO(Long id, String dataHora, String usuario, String local, String descricaoDetalhada, String caminhoAnexo) {
        this.id = id;
        this.dataHora = dataHora;
        Usuario = usuario;
        this.local = local;
        this.descricaoDetalhada = descricaoDetalhada;
        this.caminhoAnexo = caminhoAnexo;
    }

    public ReclamacaoDTO(Reclamacao reclamacao){
        this.id = reclamacao.getId();
        this.dataHora = reclamacao.getDataHora();
        Usuario = reclamacao.getUsuario().getEmailEducacional();
        this.local = reclamacao.getLocal();
        this.descricaoDetalhada = reclamacao.getDescricaoDetalhada();
        this.caminhoAnexo = reclamacao.getCaminhoAnexo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDescricaoDetalhada() {
        return descricaoDetalhada;
    }

    public void setDescricaoDetalhada(String descricaoDetalhada) {
        this.descricaoDetalhada = descricaoDetalhada;
    }

    public String getCaminhoAnexo() {
        return caminhoAnexo;
    }

    public void setCaminhoAnexo(String caminhoAnexo) {
        this.caminhoAnexo = caminhoAnexo;
    }
}