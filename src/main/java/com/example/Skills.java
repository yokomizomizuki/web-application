package com.example;

import java.math.BigDecimal;

public class Skills {
	private BigDecimal id;
	private String name;
	private String skill;

	// コンストラクタ
	public Skills(String name, String skill, BigDecimal id) {
		this.name = name;
		this.skill = skill;
		this.id = id;
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

	// setter
	public void setSkill(String skill) {
		this.skill = skill;
	}

	// getter
	public String getSkill() {
		return skill;
	}
}