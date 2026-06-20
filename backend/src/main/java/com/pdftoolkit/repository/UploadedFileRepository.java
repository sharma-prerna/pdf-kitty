package com.pdftoolkit.repository;

import com.pdftoolkit.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    List<UploadedFile> findByJobIdOrderBySortOrderAsc(UUID jobId);
}
