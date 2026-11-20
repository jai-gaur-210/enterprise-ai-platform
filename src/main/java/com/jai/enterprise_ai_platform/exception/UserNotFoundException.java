package com.jai.enterprise_ai_platform.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User with id '" + id + "' was not found");
    }
}