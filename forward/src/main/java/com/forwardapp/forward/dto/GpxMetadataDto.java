package com.forwardapp.forward.dto;

import com.forwardapp.forward.model.GpxMetadata;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record GpxMetadataDto(
    Long id,
    String filename,
    double distanceKm,
    Long durationSec,
    String startDate,
    String startTime,
    String pace,
    String startIso,
    Long distanceMeters
) {
    public static GpxMetadataDto from(GpxMetadata m) {
        Long durSec = m.getDuration() != null ? m.getDuration() : 0L;
        Double distM = m.getDistance() != null ? m.getDistance() : 0.0;
        double km = distM / 1000.0;

        String paceStr = "-";
        if (km > 0.0 && durSec > 0) {
            double secPerKm = durSec / km;
            long total = Math.round(secPerKm);
            long mm = total / 60;
            long ss = total % 60;
            paceStr = "%d:%02d".formatted(mm, ss);
        }

        var nl = Locale.forLanguageTag("nl-NL");
        var dateFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", nl);
        var timeFmt = DateTimeFormatter.ofPattern("HH:mm", nl);

        String startDate = null, startTime = null, startIso = null;

        if (m.getStartTime() != null) {
            ZoneId zone = ZoneId.systemDefault();
            ZonedDateTime zdt = m.getStartTime().atZone(zone);
            startDate = zdt.format(dateFmt);
            startTime = zdt.format(timeFmt);
            startIso  = zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        return new GpxMetadataDto(
            m.getId(),
            m.getFilename(),
            km,
            durSec,
            startDate,
            startTime,
            paceStr,
            startIso,
            distM.longValue()
        );
    }
}
