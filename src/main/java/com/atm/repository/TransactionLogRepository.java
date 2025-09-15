package com.atm.repository;

import com.atm.model.TransactionLog;
import com.atm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    // ✅ Get last 5 transactions for a user (mini statement)
    List<TransactionLog> findTop5ByUserOrderByTimestampDesc(User user);

    // ✅ Get all transactions for a user
    List<TransactionLog> findByUserOrderByTimestampDesc(User user);
}
