package com.forwardapp.forward.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class GpxMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private Double distance;
    private Long duration;
    private LocalDateTime startTime;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "gpx_bytes")
    private byte[] gpxBytes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public GpxMetadata() {}

    public GpxMetadata(String filename, Double distance, Long duration, LocalDateTime startTime, User user) {
        this.filename = filename;
        this.distance = distance;
        this.duration = duration;
        this.startTime = startTime;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public byte[] getGpxBytes() { return gpxBytes; }        // <-- getters/setters
    public void setGpxBytes(byte[] gpxBytes) { this.gpxBytes = gpxBytes; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
