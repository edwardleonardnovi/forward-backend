package com.forwardapp.forward.service;

import com.forwardapp.forward.model.User;
import java.util.List;

public interface UserService {
    List<User> findAthletesOfCoach(Long coachId);
    boolean isAthleteOfCoach(Long coachId, Long userId);
}