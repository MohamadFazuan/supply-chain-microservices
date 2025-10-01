package com.supply.chain.microservice.productservice.exception;

/**
 * Exception thrown when duplicate SKU is detected
 */
public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String message) {
        super(message);
    }

    public DuplicateSkuException(String message, Throwable cause) {
        super(message, cause);
    }
}
