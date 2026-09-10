package com.seungjig.shoppingmall.order.exception;

public class NotFoundOrderItemException extends RuntimeException {
    public NotFoundOrderItemException(String message) {
        super(message);
    }
}
