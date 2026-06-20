package com.pdftoolkit.controller;

import com.pdftoolkit.dto.JobResponse;
import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.processor.ProcessParams;
import com.pdftoolkit.service.PdfOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "PDF", description = "Core PDF operations")
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfOperationService operationService;

    @Operation(summary = "Compress a PDF (level = LOW | MEDIUM | HIGH)")
    @PostMapping(value = "/compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobResponse> compress(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "level", defaultValue = "MEDIUM") String level) {
        return accepted(operationService.submit(OperationType.COMPRESS, files,
                Map.of(ProcessParams.COMPRESSION_LEVEL, level)));
    }

    @Operation(summary = "Merge multiple PDFs (multipart order = merge order)")
    @PostMapping(value = "/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobResponse> merge(@RequestParam("files") List<MultipartFile> files) {
        return accepted(operationService.submit(OperationType.MERGE, files, Map.of()));
    }

    @Operation(summary = "Split a PDF (mode = RANGE | PAGES | EVERY, spec e.g. '1-5' or '1,3,5')")
    @PostMapping(value = "/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobResponse> split(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "mode", defaultValue = "EVERY") String mode,
            @RequestParam(value = "spec", defaultValue = "") String spec) {
        return accepted(operationService.submit(OperationType.SPLIT, files,
                Map.of(ProcessParams.SPLIT_MODE, mode, ProcessParams.SPLIT_SPEC, spec)));
    }

    private ResponseEntity<JobResponse> accepted(JobResponse body) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }
}
