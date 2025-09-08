package com.forwardapp.forward.controller;

import com.forwardapp.forward.dto.AuthRequest;
import com.forwardapp.forward.dto.AuthResponse;
import com.forwardapp.forward.dto.RegisterRequest;
import com.forwardapp.forward.model.Coach;
import com.forwardapp.forward.model.User;
import com.forwardapp.forward.model.Role;
import com.forwardapp.forward.service.CoachService;
import com.forwardapp.forward.service.JwtService;
import com.forwardapp.forward.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final CoachService coachService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, UserService userService, CoachService coachService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.userService = userService;
        this.coachService = coachService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        String role = request.getRole().toUpperCase();
        switch (role) {
            case "COACH" -> {
                Coach coach = new Coach(
                        request.getEmail(),
                        request.getUsername(),
                        passwordEncoder.encode(request.getPassword()),
                        request.getBio(),
                        request.getSpecialty()
                );
                coachService.saveCoach(coach);
                return ResponseEntity.ok("Coach registered successfully");
            }
            case "USER" -> {
                User user = new User(
                        request.getEmail(),
                        request.getUsername(),
                        passwordEncoder.encode(request.getPassword()),
                        Role.USER
                );
                userService.saveUser(user);
                return ResponseEntity.ok("User registered successfully");
            }
            default -> {
                return ResponseEntity.badRequest().body("Ongeldige rol: gebruik 'USER' of 'COACH'");
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails user = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException e) {
            e.printStackTrace();
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
