package com.forwardapp.forward.repository;

import com.forwardapp.forward.model.GpxMetadata;
import com.forwardapp.forward.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GpxMetadataRepository extends JpaRepository<GpxMetadata, Long> {
    List<GpxMetadata> findByUser(User user);
    Optional<GpxMetadata> findById(Long id);
}