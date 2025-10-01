package com.supply.chain.microservice.productservice.exception;

/**
 * Exception thrown when invalid product data is provided
 */
public class InvalidProductDataException extends RuntimeException {

    public InvalidProductDataException(String message) {
        super(message);
    }

    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
