package Ouvidoria.Senai.repositories;

import Ouvidoria.Senai.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.login.id = :loginId")
    void deleteByLoginId(@Param("loginId") Long loginId);
}
