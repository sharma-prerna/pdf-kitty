package com.pdftoolkit.service;

import com.pdftoolkit.dto.JobResponse;

import java.util.List;
import java.util.UUID;

/**
 * Read-side access to processing jobs.
 */
public interface JobService {

    JobResponse getJob(UUID id);

    List<JobResponse> listJobs(int page, int size);
}
