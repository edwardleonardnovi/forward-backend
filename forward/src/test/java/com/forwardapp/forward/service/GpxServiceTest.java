package com.forwardapp.forward.service;

import com.forwardapp.forward.dto.LatLon;
import com.forwardapp.forward.model.GpxMetadata;
import com.forwardapp.forward.model.User;
import com.forwardapp.forward.repository.GpxMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpxServiceTest {

    @Mock
    private GpxMetadataRepository repository;

    @InjectMocks
    private GpxService service;

    private User owner;
    private User other;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        other = new User();
        other.setId(2L);
        other.setEmail("other@example.com");
    }

    @Test
    void parseGpx_validTwoPoints_returnsMetaWithDurationAndDistance() {
        String xml = """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
              <trk><name>Test</name><trkseg>
                <trkpt lat="52.0907" lon="5.1214"><time>2025-04-15T10:00:00Z</time></trkpt>
                <trkpt lat="52.0910" lon="5.1220"><time>2025-04-15T10:05:00Z</time></trkpt>
              </trkseg></trk>
            </gpx>
            """;
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        GpxMetadata meta = service.parseGpx(in);

        assertNotNull(meta);
        assertNotNull(meta.getStartTime());
        assertTrue(meta.getDuration() > 0, "duration must be > 0");
        assertTrue(meta.getDistance() > 0, "distance must be > 0");
    }

    @Test
    void parseGpx_insufficientPoints_throwsRuntimeException() {
        String xml = """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
              <trk><trkseg>
                <trkpt lat="52.0907" lon="5.1214"><time>2025-04-15T10:00:00Z</time></trkpt>
              </trkseg></trk>
            </gpx>
            """;
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.parseGpx(in));
        assertTrue(ex.getMessage().contains("Fout bij GPX parsing"));
    }

    @Test
    void getByUser_delegatesToRepository() {
        GpxMetadata m = new GpxMetadata();
        m.setUser(owner);
        when(repository.findByUser(owner)).thenReturn(List.of(m));

        List<GpxMetadata> result = service.getByUser(owner);

        assertEquals(1, result.size());
        verify(repository).findByUser(owner);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteRunByIdAndUser_found_deletesAndReturnsTrue() {
        GpxMetadata m = new GpxMetadata();
        m.setId(31L);
        m.setUser(owner);

        when(repository.findById(31L)).thenReturn(Optional.of(m));

        boolean ok = service.deleteRunByIdAndUser(31L);

        assertTrue(ok);
        verify(repository).findById(31L);
        verify(repository).delete(m);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteRunByIdAndUser_missing_returnsFalse() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        boolean ok = service.deleteRunByIdAndUser(99L);

        assertFalse(ok);
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void saveMetadata_callsRepositorySave() {
        GpxMetadata m = new GpxMetadata();
        m.setUser(owner);

        service.saveMetadata(m);

        ArgumentCaptor<GpxMetadata> captor = ArgumentCaptor.forClass(GpxMetadata.class);
        verify(repository).save(captor.capture());
        assertSame(owner, captor.getValue().getUser());
        verifyNoMoreInteractions(repository);
    }

    public static class MetaWithXml extends GpxMetadata {
        private final String xml;
        public MetaWithXml(String xml) { this.xml = xml; }
        public String getGpxXml() { return xml; }
    }

    private String smallTrackXml() {
        return """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
              <trk><trkseg>
                <trkpt lat="52.0907" lon="5.1214"></trkpt>
                <trkpt lat="52.0910" lon="5.1220"></trkpt>
                <trkpt lat="52.0912" lon="5.1225"></trkpt>
              </trkseg></trk>
            </gpx>
            """;
    }

    @Test
    void getTrackLatLon_happyPath_parsesLatLon_whenUserMatches() {
        MetaWithXml meta = new MetaWithXml(smallTrackXml());
        meta.setId(31L);
        meta.setUser(owner);

        when(repository.findById(31L)).thenReturn(Optional.of(meta));

        List<LatLon> pts = service.getTrackLatLon(31L, owner);

        assertFalse(pts.isEmpty());
        assertTrue(pts.size() >= 2);
        LatLon p0 = pts.get(0);
        assertTrue(p0.lat() > 50 && p0.lat() < 60);
        verify(repository).findById(31L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getTrackLatLon_userMismatch_returnsEmpty() {
        MetaWithXml meta = new MetaWithXml(smallTrackXml());
        meta.setId(31L);
        meta.setUser(owner);

        when(repository.findById(31L)).thenReturn(Optional.of(meta));

        List<LatLon> pts = service.getTrackLatLon(31L, other);

        assertTrue(pts.isEmpty(), "should be empty when user IDs differ");
        verify(repository).findById(31L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getTrackLatLon_missingRun_returnsEmpty() {
        when(repository.findById(123L)).thenReturn(Optional.empty());

        List<LatLon> pts = service.getTrackLatLon(123L, owner);

        assertTrue(pts.isEmpty());
        verify(repository).findById(123L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getTrackLatLon_noGpxSource_returnsEmpty() {
        GpxMetadata meta = new GpxMetadata();
        meta.setId(55L);
        meta.setUser(owner);

        when(repository.findById(55L)).thenReturn(Optional.of(meta));

        List<LatLon> pts = service.getTrackLatLon(55L, owner);

        assertTrue(pts.isEmpty());
        verify(repository).findById(55L);
        verifyNoMoreInteractions(repository);
    }


    @Test
    void parseGpx_noTime_setsDurationZero_butCalculatesDistance() {
        String xml = """
            <gpx version="1.1" creator="test" xmlns="http://www.topografix.com/GPX/1/1">
              <trk><trkseg>
                <trkpt lat="52.0907" lon="5.1214"></trkpt>
                <trkpt lat="52.0910" lon="5.1220"></trkpt>
              </trkseg></trk>
            </gpx>
            """;
        InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        GpxMetadata meta = service.parseGpx(in);

        assertNotNull(meta);
        assertNull(meta.getStartTime());
        assertEquals(0L, meta.getDuration());
        assertTrue(meta.getDistance() > 0);
    }
}
