package com.forwardapp.forward.service;

import com.forwardapp.forward.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAthletesOfCoach(Long coachId);
    boolean isAthleteOfCoach(Long coachId, Long userId);

    Optional<User> findAthleteByEmail(String email);
    Optional<User> findAthleteById(Long userId);
    void linkCoachToAthlete(Long coachId, Long athleteId);
}
