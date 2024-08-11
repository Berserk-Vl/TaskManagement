package ru.sb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.sb.config.security.JwtService;
import ru.sb.model.User;
import ru.sb.model.UserRepository;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public Map<String, Object> login(Map<String, String> fields) {
        if (!fields.containsKey("email") || !fields.containsKey("password")
                || fields.get("email") == null || fields.get("password") == null) {
            throw new IllegalArgumentException("ERROR[400]: The Email and Password fields are required and cannot be null.");
        }
        try {
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(fields.get("email"), fields.get("password")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return Map.of("token", getJwt(fields.get("email")));
        } catch (AuthenticationException e) {
            throw new RuntimeException("ERROR[403]: Authentication failed.");
        }
    }

    private String getJwt(String email) {
        return jwtService.generateToken(email);
    }
}