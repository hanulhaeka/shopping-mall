package com.seungjig.shoppingmall.user.exception;

public class NotCorrectConfirmPasswordException extends RuntimeException {
    public NotCorrectConfirmPasswordException(String message) {
        super(message);
    }
}
