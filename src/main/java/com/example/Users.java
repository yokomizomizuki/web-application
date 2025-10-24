package com.example;

import java.math.BigDecimal;

public class Users {
	private BigDecimal id;
	private String name;

	// コンストラクタ
	public Users(BigDecimal id, String name) {
		this.id = id;
		this.name = name;
	}

	// setter
	public void setId(BigDecimal id) {
		this.id = id;
	}

	// getter
	public BigDecimal getId() {
		return id;
	}

	// setter
	public void setName(String name) {
		this.name = name;
	}

	// getter
	public String getName() {
		return name;
	}
}