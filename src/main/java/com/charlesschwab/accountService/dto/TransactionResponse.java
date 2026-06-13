package com.charlesschwab.accountService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
	private String eventId;
	private String accountId;
	private BigDecimal amount;
	private LocalDateTime appliedAt;

	public TransactionResponse() {
	}

	public TransactionResponse(String eventId, String accountId, BigDecimal amount, LocalDateTime appliedAt) {
		this.eventId = eventId;
		this.accountId = accountId;
		this.amount = amount;
		this.appliedAt = appliedAt;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDateTime getAppliedAt() {
		return appliedAt;
	}

	public void setAppliedAt(LocalDateTime appliedAt) {
		this.appliedAt = appliedAt;
	}
}

