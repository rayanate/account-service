package com.charlesschwab.accountService.dto;

import java.math.BigDecimal;

public class TransactionRequest {
	private String eventId;
	private BigDecimal amount;

	public TransactionRequest() {
	}

	public TransactionRequest(String eventId, BigDecimal amount) {
		this.eventId = eventId;
		this.amount = amount;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}

