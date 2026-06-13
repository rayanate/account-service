package com.charlesschwab.accountService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "applied_transactions")
public class AppliedTransaction {

	@Id
	private String eventId;

	private String accountId;
	private BigDecimal amount;
	private LocalDateTime appliedAt;

	public AppliedTransaction() {
	}

	public AppliedTransaction(String eventId, String accountId, BigDecimal amount) {
		this.eventId = eventId;
		this.accountId = accountId;
		this.amount = amount;
		this.appliedAt = LocalDateTime.now();
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

	@Override
	public String toString() {
		return "AppliedTransaction{" +
				"eventId='" + eventId + '\'' +
				", accountId='" + accountId + '\'' +
				", amount=" + amount +
				", appliedAt=" + appliedAt +
				'}';
	}
}

