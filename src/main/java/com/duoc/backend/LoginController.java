package com.duoc.backend;

import com.duoc.backend.user.MyUserDetailsService;
import com.duoc.backend.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LoginController {

    @Autowired
    private JWTAuthenticationConfig jwtAuthenticationConfig;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login");
        }

        return jwtAuthenticationConfig.getJWTToken(loginRequest.getUsername());
    }
}
