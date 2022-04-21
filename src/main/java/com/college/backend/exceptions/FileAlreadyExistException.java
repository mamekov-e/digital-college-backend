package com.college.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileAlreadyExistException extends RuntimeException{
    public FileAlreadyExistException(String message) {
        super(message);
    }
}