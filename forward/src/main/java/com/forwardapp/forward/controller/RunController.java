package com.forwardapp.forward.controller;

import com.forwardapp.forward.dto.GpxMetadataDto;
import com.forwardapp.forward.dto.RouteGeoJsonDto;
import com.forwardapp.forward.model.GpxMetadata;
import com.forwardapp.forward.model.User;
import com.forwardapp.forward.service.GpxService;
import com.forwardapp.forward.service.UserServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/runs")
@CrossOrigin(origins = "http://localhost:3000")
public class RunController {

    private final GpxService gpxService;
    private final UserServiceImpl userService;

    public RunController(GpxService gpxService, UserServiceImpl userService) {
        this.gpxService = gpxService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<GpxMetadataDto>> getRuns(Authentication auth) {
        User user = resolveUser(auth);
        List<GpxMetadata> metadata = gpxService.getByUser(user);
        List<GpxMetadataDto> dtoList = metadata.stream().map(GpxMetadataDto::from).toList();
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/{runId}")
    public ResponseEntity<?> deleteRun(@PathVariable Long runId, Authentication auth) {
        boolean deleted = gpxService.deleteRunByIdAndUser(runId);
        return deleted
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(403).body("Kan run niet verwijderen (geen toegang of niet gevonden)");
    }

    @GetMapping("/{runId}/route")
    public ResponseEntity<RouteGeoJsonDto> getRoute(@PathVariable Long runId, Authentication auth) {
        User user = resolveUser(auth);

        var points = gpxService.getTrackLatLon(runId, user);

        if (points == null || points.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var feature = RouteGeoJsonDto.from(points);
        return ResponseEntity.ok(feature);
    }

    private User resolveUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User niet gevonden"));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<GpxMetadataDto> uploadRun(
        @RequestPart("file") MultipartFile file,
        Authentication auth
    ) {
        User user = resolveUser(auth);
        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        try {
            byte[] bytes = file.getBytes();

            var meta = gpxService.parseGpx(new ByteArrayInputStream(bytes));
            meta.setUser(user);
            meta.setFilename(file.getOriginalFilename());
            meta.setGpxBytes(bytes);

            gpxService.saveMetadata(meta);
            return ResponseEntity.ok(GpxMetadataDto.from(meta));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
