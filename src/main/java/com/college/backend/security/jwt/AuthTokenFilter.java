package com.college.backend.security.jwt;


import com.college.backend.model.User;
import com.college.backend.security.SecurityConstants;
import com.college.backend.service.UserDetailsImpl;
import com.college.backend.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class AuthTokenFilter extends OncePerRequestFilter {
    public static final Logger LOG = LoggerFactory.getLogger(JwtProvider.class);

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getTokenFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
                Long userId = jwtProvider.getUserIdFromToken(jwt);

                User userDetails = userDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, Collections.emptyList());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            LOG.error("Cannot set user authentication: {}", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            LOG.error(e.getMessage());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);

        if (StringUtils.hasText(token) && token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return token.split(" ")[1];
        }
        return null;
    }
}
