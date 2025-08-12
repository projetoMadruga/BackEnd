package Ouvidoria.Senai.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@MappedSuperclass // Indica que essa classe não será uma tabela, mas será herdada por outras entidades
public abstract class Manifestacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O assunto não pode estar em branco.")
    private String local;

    private String dataHora;

    @Lob
    @NotBlank(message = "A descrição detalhada é obrigatória.")
    private String descricaoDetalhada;

    private String caminhoAnexo;

    // Relacionamento com o usuário (Login)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "O usuário é obrigatório.")
    private Login usuario;

    // Construtor vazio (necessário para JPA)
    public Manifestacao() {}

    public Manifestacao(Long id, String local, String dataHora, String descricaoDetalhada, String caminhoAnexo, Login usuario) {
        this.id = id;
        this.local = local;
        this.dataHora = dataHora;
        this.descricaoDetalhada = descricaoDetalhada;
        this.caminhoAnexo = caminhoAnexo;
        this.usuario = usuario;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
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

    public Login getUsuario() {
        return usuario;
    }

    public void setUsuario(Login usuario) {
        this.usuario = usuario;
    }
}