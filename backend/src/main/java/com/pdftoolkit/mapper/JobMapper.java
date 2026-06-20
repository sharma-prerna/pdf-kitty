package com.pdftoolkit.mapper;

import com.pdftoolkit.dto.JobResponse;
import com.pdftoolkit.dto.OutputFileResponse;
import com.pdftoolkit.entity.OutputFile;
import com.pdftoolkit.entity.ProcessingJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Maps persistence entities to their public DTO representations.
 */
@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "outputs", source = "outputs")
    JobResponse toResponse(ProcessingJob job, List<OutputFile> outputs);

    OutputFileResponse toOutputResponse(OutputFile file);

    List<OutputFileResponse> toOutputResponses(List<OutputFile> files);
}
