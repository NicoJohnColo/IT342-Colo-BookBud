package edu.cit.colo.bookbud.repository;

import edu.cit.colo.bookbud.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUserUserId(String userId);
    
    void deleteByToken(String token);
    
    void deleteByUserUserId(String userId);
}
