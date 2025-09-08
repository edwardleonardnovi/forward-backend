package com.forwardapp.forward.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coach_athlete")
public class CoachAthlete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coach_id", nullable = false)
    private Long coachId;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;

    public CoachAthlete() {}

    public CoachAthlete(Long coachId, Long athleteId) {
        this.coachId = coachId;
        this.athleteId = athleteId;
    }

    public Long getId() { return id; }
    public Long getCoachId() { return coachId; }
    public Long getAthleteId() { return athleteId; }

    public void setId(Long id) { this.id = id; }
    public void setCoachId(Long coachId) { this.coachId = coachId; }
    public void setAthleteId(Long athleteId) { this.athleteId = athleteId; }
}
