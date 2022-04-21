package com.college.backend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private boolean success;
    private String token;
    private String role;
}
