package com.omnibot.repository;

import com.omnibot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :now WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE User u SET u.isVerified = true WHERE u.email = :email")
    void markEmailVerified(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :hash WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("hash") String hash);
}
