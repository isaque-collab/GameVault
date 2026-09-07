CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL  UNIQUE,
    email VARCHAR(150) NOT NULL  UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rawg_game_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_favorites_user_game
                       UNIQUE (user_id, rawg_game_id),

    CONSTRAINT fk_favorites_user
                       FOREIGN KEY (user_id)
                       REFERENCES users(id)
                       ON DELETE CASCADE
);

CREATE TABLE wishlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rawg_game_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_wishlist_user_game
                      UNIQUE (user_id, rawg_game_id),

    CONSTRAINT fk_wishlist_user
                      FOREIGN KEY (user_id)
                      REFERENCES users(id)
                      ON DELETE CASCADE
);

CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rawg_game_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_reviews_rating
                     CHECK ( rating BETWEEN 1 AND 5),

    CONSTRAINT uk_reviews_user_game
                     UNIQUE (user_id, rawg_game_id),

    CONSTRAINT fk_reviews_user
                     FOREIGN KEY (user_id)
                     REFERENCES users(id)
                     ON DELETE CASCADE
);