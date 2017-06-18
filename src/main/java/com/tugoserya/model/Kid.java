package com.tugoserya.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class Kid {
	private final String name;
	private final LocalDate birthday;
	private final String accountId;

	@JsonCreator
	public Kid(@JsonProperty("name") String name,
			   @JsonProperty("birthday") LocalDate birthday,
			   @JsonProperty("accountId") String accountId) {
		this.name = name;
		this.birthday = birthday;
		this.accountId = accountId;
	}

	public String getName() {
		return name;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public String getAccountId() {
		return accountId;
	}
}
