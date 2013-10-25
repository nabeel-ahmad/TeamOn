package com.spikenow.enums;

public enum EnumActivityTypes {
	OPEN("open"),
	ONE_TEAM("1team"),
	TWO_TEAM("2team"),
	PRACTICE("practice");
	
	private EnumActivityTypes(String code) {
		this.code = code;
	}

	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
