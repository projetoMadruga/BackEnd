package Ouvidoria.Senai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_permissao_area_usuario", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "area"})
})
public class PermissaoAreaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Login usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAcesso nivelAcesso;

    public PermissaoAreaUsuario() {}

    public PermissaoAreaUsuario(Login usuario, Area area, NivelAcesso nivelAcesso) {
        this.usuario = usuario;
        this.area = area;
        this.nivelAcesso = nivelAcesso;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Login getUsuario() { return usuario; }
    public void setUsuario(Login usuario) { this.usuario = usuario; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }

    public NivelAcesso getNivelAcesso() { return nivelAcesso; }
    public void setNivelAcesso(NivelAcesso nivelAcesso) { this.nivelAcesso = nivelAcesso; }
}
