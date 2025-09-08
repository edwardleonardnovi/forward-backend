package com.forwardapp.forward.repository;

import com.forwardapp.forward.model.CoachAthlete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachLinkRepository extends JpaRepository<CoachAthlete, Long> {
    boolean existsByCoachIdAndAthleteId(Long coachId, Long athleteId);
    List<CoachAthlete> findAllByCoachId(Long coachId);
}
