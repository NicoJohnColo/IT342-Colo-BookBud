package edu.cit.colo.bookbud.features.users.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.colo.bookbud.features.users.entity.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);
    
    List<PasswordResetToken> findByUserUserIdAndUsedAtIsNull(String userId);
    
    void deleteByUserUserIdAndExpiresAtBefore(String userId, LocalDateTime dateTime);
}
