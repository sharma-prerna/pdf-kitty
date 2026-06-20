package com.pdftoolkit.repository;

import com.pdftoolkit.entity.ProcessingJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, UUID> {

    Page<ProcessingJob> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
