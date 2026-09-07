package com.gamevault.favorite.entity;

import com.gamevault.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorites_user_game",
                        columnNames = {"user_id", "rawg_game_id"}
                )
        }
)
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rawg_game_id", nullable = false)
    private Long rawgGameId;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public Favorite() {

    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
