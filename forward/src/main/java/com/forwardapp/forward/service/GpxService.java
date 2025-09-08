package com.forwardapp.forward.service;

import com.forwardapp.forward.dto.LatLon;
import com.forwardapp.forward.model.GpxMetadata;
import com.forwardapp.forward.model.User;
import com.forwardapp.forward.repository.GpxMetadataRepository;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GpxService {

    private final GpxMetadataRepository repository;

    public GpxService(GpxMetadataRepository repository) {
        this.repository = repository;
    }

    public GpxMetadata parseGpx(InputStream input) {
        try {
            GPX gpx = GPX.read(input);

            List<WayPoint> trackPoints = gpx.tracks()
                    .flatMap(track -> track.segments())
                    .flatMap(seg -> seg.points())
                    .toList();

            if (trackPoints.size() < 2) {
                throw new IllegalArgumentException("GPX bevat onvoldoende trackpoints");
            }

            WayPoint first = trackPoints.get(0);
            WayPoint last = trackPoints.get(trackPoints.size() - 1);

            LocalDateTime start = first.getTime()
                    .map(t -> t.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                    .orElse(null);

            LocalDateTime end = last.getTime()
                    .map(t -> t.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
                    .orElse(null);

            long durationSeconds = (start != null && end != null)
                    ? Duration.between(start, end).getSeconds()
                    : 0;

            double totalDistanceMeters = 0.0;
            for (int i = 1; i < trackPoints.size(); i++) {
                WayPoint p1 = trackPoints.get(i - 1);
                WayPoint p2 = trackPoints.get(i);
                totalDistanceMeters += haversine(
                        p1.getLatitude().doubleValue(),
                        p1.getLongitude().doubleValue(),
                        p2.getLatitude().doubleValue(),
                        p2.getLongitude().doubleValue()
                );
            }

            GpxMetadata meta = new GpxMetadata();
            meta.setStartTime(start);
            meta.setDuration(durationSeconds);
            meta.setDistance(totalDistanceMeters);
            return meta;

        } catch (Exception e) {
            throw new RuntimeException("Fout bij GPX parsing", e);
        }
    }

    @Transactional(readOnly = true)
    public List<GpxMetadata> getByUser(User user) {
        return repository.findByUser(user);
    }

    public boolean deleteRunByIdAndUser(Long runId) {
        return repository.findById(runId)
            .map(run -> {
                repository.delete(run);
                return true;
            })
            .orElse(false);
    }

    public void saveMetadata(GpxMetadata metadata) {
        repository.save(metadata);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Transactional(readOnly = true)
    public List<LatLon> getTrackLatLon(Long runId, User user) {
        Optional<GpxMetadata> opt = repository.findById(runId);
        if (opt.isEmpty()) return List.of();

        GpxMetadata meta = opt.get();

        if (meta.getUser() != null && user != null && !meta.getUser().getId().equals(user.getId())) {
            return List.of();
        }

        try (InputStream in = openGpxStream(meta)) {
            if (in == null) return List.of();

            GPX gpx = GPX.read(in);
            var pts = new ArrayList<LatLon>();

            gpx.tracks()
               .flatMap(t -> t.segments())
               .flatMap(s -> s.points())
               .forEach(p -> {
                   if (!(p.getLatitude() == null) && !(p.getLongitude() == null)) {
                       pts.add(new LatLon(
                               p.getLatitude().doubleValue(),
                               p.getLongitude().doubleValue()
                       ));
                   }
               });

            return pts;
        } catch (Exception e) {
            return List.of();
        }
    }

    private InputStream openGpxStream(GpxMetadata meta) {
        try {
            Method mBytes = tryMethod(meta.getClass(), "getGpxBytes");
            if (mBytes != null && mBytes.getReturnType() == byte[].class) {
                byte[] bytes = (byte[]) mBytes.invoke(meta);
                if (bytes != null && bytes.length > 0) {
                    return new ByteArrayInputStream(bytes);
                }
            }

            Method mXml = tryMethod(meta.getClass(), "getGpxXml");
            if (mXml != null && mXml.getReturnType() == String.class) {
                String xml = (String) mXml.invoke(meta);
                if (xml != null && !xml.isBlank()) {
                    return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                }
            }

            String path = null;
            Method mPath = tryMethod(meta.getClass(), "getGpxPath");
            if (mPath != null && mPath.getReturnType() == String.class) {
                path = (String) mPath.invoke(meta);
            } else {
                Method mFilePath = tryMethod(meta.getClass(), "getFilePath");
                if (mFilePath != null && mFilePath.getReturnType() == String.class) {
                    path = (String) mFilePath.invoke(meta);
                }
            }
            if (path != null && !path.isBlank()) {
                return Files.newInputStream(Path.of(path));
            }

        } catch (Exception ignore) {
            
        }
        return null;
    }

    private Method tryMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<GpxMetadata> getByUserId(Long userId) {
        return repository.findAllByUser_IdOrderByStartTimeDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<GpxMetadata> getByIdAndUserId(Long gpxId, Long userId) {
        return repository.findByIdAndUser_Id(gpxId, userId);
    }
}
