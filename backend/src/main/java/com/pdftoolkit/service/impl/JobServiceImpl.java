package com.pdftoolkit.service.impl;

import com.pdftoolkit.dto.JobResponse;
import com.pdftoolkit.entity.ProcessingJob;
import com.pdftoolkit.exception.JobNotFoundException;
import com.pdftoolkit.mapper.JobMapper;
import com.pdftoolkit.repository.OutputFileRepository;
import com.pdftoolkit.repository.ProcessingJobRepository;
import com.pdftoolkit.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

    private final ProcessingJobRepository jobRepository;
    private final OutputFileRepository outputFileRepository;
    private final JobMapper jobMapper;

    @Override
    public JobResponse getJob(UUID id) {
        ProcessingJob job = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
        return jobMapper.toResponse(job, outputFileRepository.findByJobIdOrderBySortOrderAsc(id));
    }

    @Override
    public List<JobResponse> listJobs(int page, int size) {
        return jobRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(job -> jobMapper.toResponse(job,
                        outputFileRepository.findByJobIdOrderBySortOrderAsc(job.getId())))
                .getContent();
    }
}
