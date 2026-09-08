package com.gamevault.listadesejos.entity;


import com.gamevault.user.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "wishlist",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wishlist_user_game",
                        columnNames = {"user_id", "rawg_game_id"}
                )
        }
)
public class ItemListaDesejos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    @Column(name = "rawg_game_id", nullable = false)
    private Long rawgGameId;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public ItemListaDesejos() {

    }

    public Long getId() {
        return id;
    }

    public Usuario getUser() {
        return usuario;
    }

    public void setUser(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getRawgGameId() {
        return rawgGameId;
    }

    public void setRawgGameId(Long rawgGameId) {
        this.rawgGameId = rawgGameId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
