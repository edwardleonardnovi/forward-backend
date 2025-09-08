package com.forwardapp.forward.service;

import com.forwardapp.forward.model.User;
import com.forwardapp.forward.repository.CoachLinkRepository;
import com.forwardapp.forward.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoachLinkRepository coachLinkRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           CoachLinkRepository coachLinkRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.coachLinkRepository = coachLinkRepository;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean checkCredentials(String email, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAthletesOfCoach(Long coachId) {
        var links = coachLinkRepository.findAllByCoachId(coachId);
        var athleteIds = links.stream().map(l -> l.getAthleteId()).toList();
        return athleteIds.isEmpty() ? List.of() : userRepository.findAllById(athleteIds);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAthleteOfCoach(Long coachId, Long userId) {
        return coachLinkRepository.existsByCoachIdAndAthleteId(coachId, userId);
    }
}
