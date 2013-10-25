package com.spikenow.model;

public class Password {
	private String password;
	private String password2;
	private String access_token;

	public Password() {
		
	}
	
	public Password(String token) {
		setAccess_token(token);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

	public String getPassword2() {
		return password2;
	}	
}
