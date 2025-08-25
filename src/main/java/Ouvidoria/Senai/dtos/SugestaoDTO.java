package Ouvidoria.Senai.dtos;

import Ouvidoria.Senai.entities.Sugestao;

public class SugestaoDTO {

    private Long id;
    private String dataHora;
    private String local;
    private String descricaoDetalhada;
    private String caminhoAnexo;

    public SugestaoDTO() {

    }

    public SugestaoDTO(Long id,String dataHora, String local,
                       String descricaoDetalhada,
                       String caminhoAnexo) {
        this.id = id;
        this.dataHora = dataHora;
        this.local = local;
        this.descricaoDetalhada = descricaoDetalhada;
        this.caminhoAnexo = caminhoAnexo;
    }




    public SugestaoDTO(Sugestao sugestao) {
        this.id = sugestao.getId();
        this.dataHora = sugestao.getDataHora();
        this.local = sugestao.getLocal();
        this.descricaoDetalhada = sugestao.getDescricaoDetalhada();
        this.caminhoAnexo = sugestao.getCaminhoAnexo();

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
