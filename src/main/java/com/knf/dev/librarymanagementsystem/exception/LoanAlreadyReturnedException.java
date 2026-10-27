package com.knf.dev.librarymanagementsystem.exception;

public class LoanAlreadyReturnedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LoanAlreadyReturnedException(String message) {
		super(message);
	}

}
