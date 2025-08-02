package com.eaglebank.repository;

import com.eaglebank.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :userId")
    @NonNull
    Optional<User> findById(@Param("userId") String userId);

    boolean existsByEmail(String email);
} 