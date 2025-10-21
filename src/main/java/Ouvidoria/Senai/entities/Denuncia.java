package Ouvidoria.Senai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_denuncia")
public class Denuncia extends Manifestacao {
	
    @Enumerated(EnumType.STRING)
    private StatusDenuncia status = StatusDenuncia.PENDENTE;
    
    @Lob
    private String observacao;
    
    public StatusDenuncia getStatus() {
        return status;
    }
    
    public void setStatus(StatusDenuncia status) {
        this.status = status;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}