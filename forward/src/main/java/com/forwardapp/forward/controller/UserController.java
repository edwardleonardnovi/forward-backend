package com.forwardapp.forward.controller;

import com.forwardapp.forward.dto.GpxMetadataDto;
import com.forwardapp.forward.model.GpxMetadata;
import com.forwardapp.forward.model.User;
import com.forwardapp.forward.service.GpxService;
import com.forwardapp.forward.service.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserServiceImpl userService;
    private final GpxService gpxService;

    public UserController(UserServiceImpl userService, GpxService gpxService) {
        this.userService = userService;
        this.gpxService = gpxService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(user);
    }

    @PostMapping("/upload-gpx")
    public ResponseEntity<String> uploadGpx(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (file.isEmpty()) return ResponseEntity.badRequest().body("Leeg bestand");

        try (InputStream input = file.getInputStream()) {
            var metadata = gpxService.parseGpx(input);
            metadata.setFilename(file.getOriginalFilename());
            metadata.setUser(user);
            gpxService.saveMetadata(metadata);
            return ResponseEntity.ok("GPX uploaded and parsed successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed");
        }
    }
}
