package app.pet_pode_back.repository;

import app.pet_pode_back.model.PasswordResetToken;
import app.pet_pode_back.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
}
