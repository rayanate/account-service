package com.charlesschwab.accountService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

	@Id
	private String accountId;

	private String name;
	private LocalDateTime createdAt;

	public Account() {
	}

	public Account(String accountId, String name) {
		this.accountId = accountId;
		this.name = name;
		this.createdAt = LocalDateTime.now();
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Account{" +
				"accountId='" + accountId + '\'' +
				", name='" + name + '\'' +
				", createdAt=" + createdAt +
				'}';
	}
}

