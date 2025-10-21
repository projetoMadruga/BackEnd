package Ouvidoria.Senai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_reclamacao")
public class Reclamacao extends Manifestacao {

    @Enumerated(EnumType.STRING)
    private StatusReclamacao status = StatusReclamacao.PENDENTE; // Status padrão é PENDENTE
    
    @Lob
    private String observacao; // Observação da manutenção sobre a reclamação
    
    @Enumerated(EnumType.STRING)
    private TipoReclamacao tipoReclamacao; // Tipo de reclamação (MANUTENCAO ou ADMINISTRACAO)
    
    public StatusReclamacao getStatus() {
        return status;
    }
    
    public void setStatus(StatusReclamacao status) {
        this.status = status;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public TipoReclamacao getTipoReclamacao() {
        return tipoReclamacao;
    }
    
    public void setTipoReclamacao(TipoReclamacao tipoReclamacao) {
        this.tipoReclamacao = tipoReclamacao;
    }
}