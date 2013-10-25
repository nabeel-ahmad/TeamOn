package com.spikenow.dao;

import com.spikenow.enums.EnumServerError;

public class TeamOnException extends Exception {
	
	private static final long serialVersionUID = 4645086551709740633L;
	private EnumServerError error;

	public TeamOnException(EnumServerError error) {
		super();
		this.error = error;
	}

	public EnumServerError getError() {
		return error;
	}

	public void setError(EnumServerError error) {
		this.error = error;
	}

}
