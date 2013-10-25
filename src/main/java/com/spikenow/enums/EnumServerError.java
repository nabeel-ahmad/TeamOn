package com.spikenow.enums;

public enum EnumServerError {
	INVALID_REQUEST(101, "Invalid request"), 
	ACCESS_DENIED(102, "Access restricted"),
	INVALID_USER(103, "User does not exist"), 
	RECORD_NOT_FOUND(104, "Record does not exist");
	
	private EnumServerError(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	private int code;
	private String message;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
