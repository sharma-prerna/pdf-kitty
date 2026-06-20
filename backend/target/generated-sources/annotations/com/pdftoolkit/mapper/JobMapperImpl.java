package com.pdftoolkit.mapper;

import com.pdftoolkit.dto.JobResponse;
import com.pdftoolkit.dto.OutputFileResponse;
import com.pdftoolkit.entity.JobStatus;
import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.entity.OutputFile;
import com.pdftoolkit.entity.ProcessingJob;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-14T18:01:38+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class JobMapperImpl implements JobMapper {

    @Override
    public JobResponse toResponse(ProcessingJob job, List<OutputFile> outputs) {
        if ( job == null && outputs == null ) {
            return null;
        }

        UUID id = null;
        OperationType operationType = null;
        JobStatus status = null;
        String originalFileName = null;
        String outputFileName = null;
        String errorMessage = null;
        Instant createdAt = null;
        Instant updatedAt = null;
        Instant processingStartedAt = null;
        Instant completedAt = null;
        if ( job != null ) {
            id = job.getId();
            operationType = job.getOperationType();
            status = job.getStatus();
            originalFileName = job.getOriginalFileName();
            outputFileName = job.getOutputFileName();
            errorMessage = job.getErrorMessage();
            createdAt = job.getCreatedAt();
            updatedAt = job.getUpdatedAt();
            processingStartedAt = job.getProcessingStartedAt();
            completedAt = job.getCompletedAt();
        }
        List<OutputFileResponse> outputs1 = null;
        outputs1 = toOutputResponses( outputs );

        JobResponse jobResponse = new JobResponse( id, operationType, status, originalFileName, outputFileName, errorMessage, createdAt, updatedAt, processingStartedAt, completedAt, outputs1 );

        return jobResponse;
    }

    @Override
    public OutputFileResponse toOutputResponse(OutputFile file) {
        if ( file == null ) {
            return null;
        }

        UUID id = null;
        String fileName = null;
        long fileSize = 0L;
        String mimeType = null;

        id = file.getId();
        fileName = file.getFileName();
        fileSize = file.getFileSize();
        mimeType = file.getMimeType();

        OutputFileResponse outputFileResponse = new OutputFileResponse( id, fileName, fileSize, mimeType );

        return outputFileResponse;
    }

    @Override
    public List<OutputFileResponse> toOutputResponses(List<OutputFile> files) {
        if ( files == null ) {
            return null;
        }

        List<OutputFileResponse> list = new ArrayList<OutputFileResponse>( files.size() );
        for ( OutputFile outputFile : files ) {
            list.add( toOutputResponse( outputFile ) );
        }

        return list;
    }
}
