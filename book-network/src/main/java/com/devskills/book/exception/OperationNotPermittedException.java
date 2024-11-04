package com.devskills.book.exception;

public class OperationNotPermittedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OperationNotPermittedException(String msg) {
		super(msg);
	}

}
