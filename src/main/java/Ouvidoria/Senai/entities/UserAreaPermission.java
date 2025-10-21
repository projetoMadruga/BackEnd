package Ouvidoria.Senai.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_user_area_permission", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "area"})
})
public class UserAreaPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private Login user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel;

    public UserAreaPermission() {}

    public UserAreaPermission(Login user, Area area, AccessLevel accessLevel) {
        this.user = user;
        this.area = area;
        this.accessLevel = accessLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Login getUser() { return user; }
    public void setUser(Login user) { this.user = user; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }

    public AccessLevel getAccessLevel() { return accessLevel; }
    public void setAccessLevel(AccessLevel accessLevel) { this.accessLevel = accessLevel; }
}
