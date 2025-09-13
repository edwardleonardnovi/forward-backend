package com.forwardapp.forward.controller;

import com.forwardapp.forward.dto.GpxMetadataDto;
import com.forwardapp.forward.model.Coach;
import com.forwardapp.forward.service.CoachService;
import com.forwardapp.forward.service.GpxService;
import com.forwardapp.forward.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('COACH')")
public class CoachController {

    private final UserServiceImpl userService;
    private final GpxService gpxService;
    private final CoachService coachService;

    public CoachController(UserServiceImpl userService, GpxService gpxService, CoachService coachService) {
        this.userService = userService;
        this.gpxService = gpxService;
        this.coachService = coachService;
    }

    @PostMapping("/athletes")
    public UserDto addAthlete(@AuthenticationPrincipal(expression = "username") String coachEmail,
                              @RequestBody AddAthleteRequest req) {
        Long coachId = coachService.findByEmail(coachEmail)
                .map(Coach::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach niet gevonden"));

        var athleteOpt =
                (req.athleteId() != null)
                        ? userService.findAthleteById(req.athleteId())
                        : (req.email() != null && !req.email().isBlank()
                            ? userService.findAthleteByEmail(req.email().trim())
                            : null);

        if (athleteOpt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geef een email of athleteId op.");
        }

        var athlete = athleteOpt.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Athlete niet gevonden"));

        userService.linkCoachToAthlete(coachId, athlete.getId());

        return new UserDto(athlete.getId(), athlete.getEmail());
    }

    @GetMapping("/athletes")
    public List<UserDto> listAthletes(@AuthenticationPrincipal(expression = "username") String coachEmail) {
        Long coachId = coachService.findByEmail(coachEmail)
                .map(Coach::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach niet gevonden"));

        return userService.findAthletesOfCoach(coachId).stream()
                .map(u -> new UserDto(u.getId(), u.getEmail()))
                .toList();
    }

    @GetMapping("/athletes/{userId}/runs")
    public List<GpxMetadataDto> runsOfAthlete(@PathVariable Long userId,
                                              @AuthenticationPrincipal(expression = "username") String coachEmail) {
        Long coachId = coachService.findByEmail(coachEmail)
                .map(Coach::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach niet gevonden"));

        if (!userService.isAthleteOfCoach(coachId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niet toegestaan voor deze atleet");
        }
        return gpxService.getByUserId(userId).stream().map(GpxMetadataDto::from).toList();
    }

    @GetMapping("/athletes/{userId}/runs/{gpxId}")
    public GpxMetadataDto runOfAthlete(@PathVariable Long userId,
                                       @PathVariable Long gpxId,
                                       @AuthenticationPrincipal(expression = "username") String coachEmail) {
        Long coachId = coachService.findByEmail(coachEmail)
                .map(Coach::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach niet gevonden"));

        if (!userService.isAthleteOfCoach(coachId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niet toegestaan voor deze atleet");
        }

        var meta = gpxService.getByIdAndUserId(gpxId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run niet gevonden"));
        return GpxMetadataDto.from(meta);
    }

    public record UserDto(Long id, String email) {}
}
