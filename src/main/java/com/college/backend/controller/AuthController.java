package com.college.backend.controller;

import com.college.backend.model.User;
import com.college.backend.payload.request.LoginRequest;
import com.college.backend.payload.request.SignupRequest;
import com.college.backend.payload.response.JwtResponse;
import com.college.backend.payload.response.MessageResponse;
import com.college.backend.security.SecurityConstants;
import com.college.backend.security.jwt.JwtProvider;
import com.college.backend.service.UserDetailsImpl;
import com.college.backend.service.UserService;
import com.college.backend.validations.ResponseErrorValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
@PreAuthorize("permitAll()")
public class AuthController {

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ResponseErrorValidation errorValidation;
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser (@Valid @RequestBody SignupRequest signupRequest,
                                                BindingResult bindingResult) {
        ResponseEntity<Object> errors = errorValidation.mapValidationService(bindingResult);

        if (!ObjectUtils.isEmpty(errors)) return errors;

        User user = userService.createUser(signupRequest);

        return ResponseEntity.ok(new MessageResponse("User " + user.getFirstName() + " registered successfully"));
    }

    @PostMapping("/signin")
    public ResponseEntity<Object> authenticateUser (@Valid @RequestBody LoginRequest loginRequest,
                                                    BindingResult bindingResult) {
        ResponseEntity<Object> errors = errorValidation.mapValidationService(bindingResult);

        if (!ObjectUtils.isEmpty(errors)) return errors;

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String jwt = SecurityConstants.TOKEN_PREFIX + jwtProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtResponse(true, jwt, roles.get(0)));
    }
}
