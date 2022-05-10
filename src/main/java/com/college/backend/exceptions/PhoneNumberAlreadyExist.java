package com.college.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PhoneNumberAlreadyExist extends RuntimeException {
    public PhoneNumberAlreadyExist(String message) {
        super(message);
    }
}
