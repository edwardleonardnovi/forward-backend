package com.forwardapp.forward.exception;

public class RunNotFoundException extends RuntimeException {
    public RunNotFoundException(Long id) {
        super("Run not found with id " + id);
    }
}
