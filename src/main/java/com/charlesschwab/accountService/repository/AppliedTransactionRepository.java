package com.charlesschwab.accountService.repository;

import com.charlesschwab.accountService.entity.AppliedTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AppliedTransactionRepository extends JpaRepository<AppliedTransaction, String> {
	List<AppliedTransaction> findByAccountId(String accountId);

	Page<AppliedTransaction> findByAccountIdOrderByAppliedAtDesc(String accountId, Pageable pageable);

	Long countByAccountIdAndAmountGreaterThan(String accountId, BigDecimal amount);

	Long countByAccountIdAndAmountLessThan(String accountId, BigDecimal amount);
}

