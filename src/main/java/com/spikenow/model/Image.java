package com.spikenow.model;

import org.springframework.web.multipart.MultipartFile;

public class Image {
	private String access_token;
	private String authProvider = "teamonapp.com";
	private MultipartFile file;

	public Image() {
		
	}
	
	public Image(String token) {
		setAccess_token(token);
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getAccess_token() {
		return access_token;
	}

	public String getAuthProvider() {
		return authProvider;
	}

	public void setAuthProvider(String authProvider) {
		this.authProvider = authProvider;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

}
