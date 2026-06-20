package com.pdftoolkit.repository;

import com.pdftoolkit.entity.OutputFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutputFileRepository extends JpaRepository<OutputFile, UUID> {

    List<OutputFile> findByJobIdOrderBySortOrderAsc(UUID jobId);
}
