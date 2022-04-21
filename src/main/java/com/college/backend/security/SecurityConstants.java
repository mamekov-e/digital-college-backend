package com.college.backend.security;

public class SecurityConstants {
    public static final String SIGN_UP_URL = "/api/auth/**";
    public static final String SECRET = "SecretKey";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
    public static final long EXPIRATION_TIME = 600_000_000; // 17hours
    public static final String ID_SCAN = "id-scan";
    public static final String ATTESTATION_SCAN = "attestation-scan";
    public static final String ACCEPTED_STATUS = "accepted";
    public static final String DECLINED_STATUS = "declined";
    public static final String DEFAULT_STATUS = "unchecked";


}
