package com.franco.backend.repository;

import com.franco.backend.entity.RefreshToken;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByRevokedFalse();
}
