package com.charlesschwab.accountService.controller;

import com.charlesschwab.accountService.dto.AccountDetailResponse;
import com.charlesschwab.accountService.dto.BalanceResponse;
import com.charlesschwab.accountService.dto.TransactionRequest;
import com.charlesschwab.accountService.dto.TransactionResponse;
import com.charlesschwab.accountService.entity.Account;
import com.charlesschwab.accountService.entity.AppliedTransaction;
import com.charlesschwab.accountService.repository.AccountRepository;
import com.charlesschwab.accountService.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
public class TransactionController {

	private final TransactionService transactionService;
	private final AccountRepository accountRepository;
	private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

	public TransactionController(TransactionService transactionService, AccountRepository accountRepository) {
		this.transactionService = transactionService;
		this.accountRepository = accountRepository;
	}

	@PostMapping("/{accountId}/transactions")
	public ResponseEntity<AppliedTransaction> applyTransaction(
			@PathVariable String accountId,
			@RequestBody TransactionRequest request) {
		log.info("request.applyTransaction accountId={} eventId={} amount={}", accountId, request.getEventId(), request.getAmount());

		AppliedTransaction transaction = transactionService.applyTransaction(
				request.getEventId(),
				accountId,
				request.getAmount()
		);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
	}

	@GetMapping("/{accountId}/balance")
	public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
		log.info("request.getBalance accountId={}", accountId);
		BigDecimal balance = transactionService.getBalance(accountId);
		log.info("response.getBalance accountId={} balance={}", accountId, balance);
		return ResponseEntity.ok(new BalanceResponse(accountId, balance));
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<AccountDetailResponse> getAccountDetails(@PathVariable String accountId) {
		log.info("request.getAccountDetails accountId={}", accountId);
		Account account = accountRepository.findById(accountId)
				.orElse(new Account(accountId, "Unknown"));
		
		BigDecimal balance = transactionService.getBalance(accountId);
		List<AppliedTransaction> recentTransactions = transactionService.getRecentTransactions(accountId, 10);
		
		List<TransactionResponse> transactionResponses = recentTransactions.stream()
				.map(t -> new TransactionResponse(t.getEventId(), t.getAccountId(), t.getAmount(), t.getAppliedAt()))
				.collect(Collectors.toList());
		
		AccountDetailResponse response = new AccountDetailResponse(
				account.getAccountId(),
				account.getName(),
				balance,
				account.getCreatedAt(),
				transactionResponses
		);
		log.info("response.getAccountDetails accountId={} transactionsReturned={}", accountId, transactionResponses.size());
		return ResponseEntity.ok(response);
	}
}

