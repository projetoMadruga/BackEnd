package Ouvidoria.Senai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_sugestao")
public class Sugestao extends Manifestacao{

    @Enumerated(EnumType.STRING)
    private StatusSugestao status = StatusSugestao.PENDENTE;
    
    @Lob
    private String observacao;
    
    public StatusSugestao getStatus() {
        return status;
    }
    
    public void setStatus(StatusSugestao status) {
        this.status = status;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}