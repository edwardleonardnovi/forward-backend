package com.forwardapp.forward.service;

import com.forwardapp.forward.model.Coach;
import com.forwardapp.forward.repository.CoachRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CoachService implements UserDetailsService {

    private final CoachRepository coachRepository;
    private final PasswordEncoder passwordEncoder;

    public CoachService(CoachRepository coachRepository,
                        PasswordEncoder passwordEncoder) {
        this.coachRepository = coachRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Coach coach = coachRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Trainer niet gevonden"));
        return new org.springframework.security.core.userdetails.User(
                coach.getEmail(),
                coach.getPassword(),
                coach.getRole().getAuthorities()
        );
    }

    public void saveCoach(Coach coach) {
       coachRepository.save(coach);
    }

    public Coach register(Coach coach) {
        coach.setPassword(passwordEncoder.encode(coach.getPassword()));
        return coachRepository.save(coach);
    }

    public boolean checkCredentials(String email, String rawPassword) {
        Optional<Coach> optionalCoach = coachRepository.findByEmail(email);
        return optionalCoach
                .map(coach -> passwordEncoder.matches(rawPassword, coach.getPassword()))
                .orElse(false);
    }

    public Optional<Coach> findByEmail(String email) {
        return coachRepository.findByEmail(email);
    }
}
