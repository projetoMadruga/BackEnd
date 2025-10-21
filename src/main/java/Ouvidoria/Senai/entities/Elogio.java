package Ouvidoria.Senai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_elogio")
public class Elogio extends Manifestacao {

    @Enumerated(EnumType.STRING)
    private StatusElogio status = StatusElogio.PENDENTE;
    
    @Lob
    private String observacao;
    
    public StatusElogio getStatus() {
        return status;
    }
    
    public void setStatus(StatusElogio status) {
        this.status = status;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}

