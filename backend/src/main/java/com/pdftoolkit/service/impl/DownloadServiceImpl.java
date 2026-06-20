package com.pdftoolkit.service.impl;

import com.pdftoolkit.entity.JobStatus;
import com.pdftoolkit.entity.OutputFile;
import com.pdftoolkit.entity.ProcessingJob;
import com.pdftoolkit.exception.JobNotFoundException;
import com.pdftoolkit.exception.ProcessingException;
import com.pdftoolkit.repository.OutputFileRepository;
import com.pdftoolkit.repository.ProcessingJobRepository;
import com.pdftoolkit.service.DownloadService;
import com.pdftoolkit.storage.StorageService;
import com.pdftoolkit.util.ZipUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DownloadServiceImpl implements DownloadService {

    private final ProcessingJobRepository jobRepository;
    private final OutputFileRepository outputFileRepository;
    private final StorageService storageService;

    @Override
    public DownloadPayload resolve(UUID jobId) {
        ProcessingJob job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
        if (job.getStatus() != JobStatus.COMPLETED) {
            throw new ProcessingException("Job " + jobId + " is not ready for download (status " + job.getStatus() + ")");
        }

        List<OutputFile> outputs = outputFileRepository.findByJobIdOrderBySortOrderAsc(jobId);
        if (outputs.isEmpty()) {
            throw new ProcessingException("Job " + jobId + " has no output files");
        }

        if (outputs.size() == 1) {
            OutputFile output = outputs.get(0);
            Resource resource = storageService.loadAsResource(output.getFilePath());
            return new DownloadPayload(resource, output.getFileName(), output.getMimeType(), output.getFileSize());
        }
        return zipBundle(outputs);
    }

    private DownloadPayload zipBundle(List<OutputFile> outputs) {
        List<ZipUtil.Entry> entries = outputs.stream()
                .map(o -> new ZipUtil.Entry(o.getFileName(), storageService.resolve(o.getFilePath())))
                .toList();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipUtil.zip(entries, out);
        byte[] bytes = out.toByteArray();
        return new DownloadPayload(new ByteArrayResource(bytes), "results.zip", "application/zip", bytes.length);
    }
}
