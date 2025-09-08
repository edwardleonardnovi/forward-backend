package com.forwardapp.forward.security;

import com.forwardapp.forward.model.Coach;
import com.forwardapp.forward.model.User;
import com.forwardapp.forward.repository.CoachRepository;
import com.forwardapp.forward.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Primary
@Service
public class CombinedUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CoachRepository coachRepository;

    public CombinedUserDetailsService(UserRepository userRepository,
                                      CoachRepository coachRepository) {
        this.userRepository = userRepository;
        this.coachRepository = coachRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var coachOpt = coachRepository.findByEmail(email);
        if (coachOpt.isPresent()) {
            Coach c = coachOpt.get();
            return org.springframework.security.core.userdetails.User
                    .withUsername(c.getEmail())
                    .password(c.getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_COACH"))
                    .build();
        }
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            return org.springframework.security.core.userdetails.User
                    .withUsername(u.getEmail())
                    .password(u.getPassword()) 
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build();
        }
        throw new UsernameNotFoundException("No user/coach with email: " + email);
    }
}