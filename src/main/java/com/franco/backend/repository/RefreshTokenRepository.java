package com.franco.backend.repository;

import com.franco.backend.entity.RefreshToken;
import com.franco.backend.entity.User;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByRevokedFalse();

    List<RefreshToken> findByUser(User user);

    List<RefreshToken> findByRevokedTrue();

}
