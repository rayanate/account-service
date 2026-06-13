package com.charlesschwab.accountService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AccountDetailResponse {
	private String accountId;
	private String name;
	private BigDecimal balance;
	private LocalDateTime createdAt;
	private List<TransactionResponse> recentTransactions;

	public AccountDetailResponse() {
	}

	public AccountDetailResponse(String accountId, String name, BigDecimal balance, LocalDateTime createdAt, List<TransactionResponse> recentTransactions) {
		this.accountId = accountId;
		this.name = name;
		this.balance = balance;
		this.createdAt = createdAt;
		this.recentTransactions = recentTransactions;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<TransactionResponse> getRecentTransactions() {
		return recentTransactions;
	}

	public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
		this.recentTransactions = recentTransactions;
	}
}

