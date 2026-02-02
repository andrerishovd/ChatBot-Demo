package com.example.demo.Authentication;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

public class AuthenticationService {
    static Dotenv dotenv = Dotenv.load();

    private static final String AUTH_TOKEN_HEADER_NAME = dotenv.get("API_KEY");
    private static final String AUTH_TOKEN = dotenv.get("API_SECRET");

    public static Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if(apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
            throw new BadCredentialsException("Invalid API key");
        }

        return new ApiAuthentication(apiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}
