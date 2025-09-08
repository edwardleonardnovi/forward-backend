package com.forwardapp.forward.dto;

import java.util.ArrayList;
import java.util.List;

public record RouteGeoJsonDto(
        String type,
        Geometry geometry,
        List<Double> bbox
) {
    public static RouteGeoJsonDto from(List<LatLon> pts) {
        var coords = new ArrayList<List<Double>>(pts.size());
        double minLat = Double.POSITIVE_INFINITY, minLon = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY, maxLon = Double.NEGATIVE_INFINITY;

        for (LatLon p : pts) {
            coords.add(List.of(p.lon(), p.lat()));
            if (p.lat() < minLat) minLat = p.lat();
            if (p.lat() > maxLat) maxLat = p.lat();
            if (p.lon() < minLon) minLon = p.lon();
            if (p.lon() > maxLon) maxLon = p.lon();
        }

        var bbox = List.of(minLon, minLat, maxLon, maxLat);
        return new RouteGeoJsonDto("Feature", new Geometry("LineString", coords), bbox);
    }

    public static record Geometry(String type, List<List<Double>> coordinates) {}
}
