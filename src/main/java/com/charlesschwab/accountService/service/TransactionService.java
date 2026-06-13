package com.charlesschwab.accountService.service;

import com.charlesschwab.accountService.entity.AppliedTransaction;
import com.charlesschwab.accountService.repository.AppliedTransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

	private final AppliedTransactionRepository repository;
	private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

	public TransactionService(AppliedTransactionRepository repository) {
		this.repository = repository;
	}

	/**
	 * Apply a transaction idempotently based on eventId.
	 * If the eventId already exists (duplicate), treat it as a no-op success.
	 * This is the backstop for the Gateway's retry-safety.
	 *
	 * @param eventId Unique event identifier
	 * @param accountId The account ID
	 * @param amount The transaction amount
	 * @return The applied transaction (newly created or existing)
	 */
	@Transactional
	public AppliedTransaction applyTransaction(String eventId, String accountId, BigDecimal amount) {
		log.debug("attempting.applyTransaction eventId={} accountId={} amount={}", eventId, accountId, amount);
		try {
			AppliedTransaction transaction = new AppliedTransaction(eventId, accountId, amount);
			AppliedTransaction saved = repository.save(transaction);
			log.info("applied.transaction saved eventId={} accountId={} amount={}", saved.getEventId(), saved.getAccountId(), saved.getAmount());
			return saved;
		} catch (DataIntegrityViolationException e) {
			// Duplicate eventId - treat as idempotent no-op success
			log.warn("duplicate.eventId detected: {} - returning existing record", eventId);
			AppliedTransaction existing = repository.findById(eventId).orElse(null);
			if (existing != null) {
				log.info("applied.transaction existing eventId={} accountId={} amount={}", existing.getEventId(), existing.getAccountId(), existing.getAmount());
			}
			// The transaction was already applied, return existing reference (or null if not found)
			return existing != null ? existing : repository.getReferenceById(eventId);
		}
	}

	/**
	 * Compute balance for an account: Σ credits − Σ debits on read.
	 * Credits are positive amounts, debits are negative amounts.
	 *
	 * @param accountId The account ID
	 * @return The balance (sum of all transactions)
	 */
	@Transactional(readOnly = true)
	public BigDecimal getBalance(String accountId) {
		List<AppliedTransaction> transactions = repository.findByAccountId(accountId);
		BigDecimal balance = transactions.stream()
				.map(AppliedTransaction::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		log.info("computed.balance accountId={} balance={} transactions={}", accountId, balance, transactions.size());
		return balance;
	}

	/**
	 * Get recent transactions for an account (limited to 10).
	 *
	 * @param accountId The account ID
	 * @return List of recent transactions, ordered by appliedAt descending
	 */
	@Transactional(readOnly = true)
	public List<AppliedTransaction> getRecentTransactions(String accountId, int limit) {
		Pageable pageable = PageRequest.of(0, limit);
		List<AppliedTransaction> recent = repository.findByAccountIdOrderByAppliedAtDesc(accountId, pageable).getContent();
		log.info("recent.transactions accountId={} returned={} limit={}", accountId, recent.size(), limit);
		return recent;
	}
}

