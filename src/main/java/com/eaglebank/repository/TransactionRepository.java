package com.eaglebank.repository;

import com.eaglebank.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("SELECT t FROM Transaction t WHERE t.bankAccount.accountNumber = :accountNumber ORDER BY t.createdTimestamp DESC")
    List<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE t.bankAccount.accountNumber = :accountNumber AND t.id = :transactionId")
    Optional<Transaction> findByAccountNumberAndTransactionId(@Param("accountNumber") String accountNumber, 
                                                           @Param("transactionId") String transactionId);
} 