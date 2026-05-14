package edu.cit.colo.bookbud.features.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.colo.bookbud.features.auth.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUserUserId(String userId);
    
    void deleteByToken(String token);
    
    void deleteByUserUserId(String userId);
}
