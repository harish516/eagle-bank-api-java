package com.eaglebank.repository;

import com.eaglebank.domain.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    @Query("SELECT ba FROM BankAccount ba WHERE ba.accountNumber = :accountNumber")
    Optional<BankAccount> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT ba FROM BankAccount ba WHERE ba.user.id = :userId")
    List<BankAccount> findByUserId(@Param("userId") String userId);

    boolean existsByAccountNumber(String accountNumber);
} 