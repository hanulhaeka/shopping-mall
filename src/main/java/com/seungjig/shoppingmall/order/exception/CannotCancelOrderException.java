package com.seungjig.shoppingmall.order.exception;

public class CannotCancelOrderException extends RuntimeException {
    public CannotCancelOrderException(String message) {
        super(message);
    }
}
