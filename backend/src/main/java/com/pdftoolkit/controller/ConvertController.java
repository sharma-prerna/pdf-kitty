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

@Tag(name = "Convert", description = "Document format conversions")
@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
public class ConvertController {

    private final PdfOperationService operationService;

    @Operation(summary = "Convert a PDF to DOCX (text-only)")
    @PostMapping(value = "/pdf-to-word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobResponse> pdfToWord(@RequestParam("files") List<MultipartFile> files) {
        return accepted(operationService.submit(OperationType.PDF_TO_WORD, files, Map.of()));
    }

    @Operation(summary = "Combine images into a single PDF (multipart order preserved)")
    @PostMapping(value = "/image-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobResponse> imageToPdf(@RequestParam("files") List<MultipartFile> files) {
        return accepted(operationService.submit(OperationType.IMAGE_TO_PDF, files, Map.of()));
    }

    @Operation(summary = "Render PDF pages to images (format = JPG | PNG, dpi = N)")
    @PostMapping(value = "/pdf-to-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobResponse> pdfToImage(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "format", defaultValue = "PNG") String format,
            @RequestParam(value = "dpi", defaultValue = "150") int dpi) {
        return accepted(operationService.submit(OperationType.PDF_TO_IMAGE, files,
                Map.of(ProcessParams.IMAGE_FORMAT, format, ProcessParams.DPI, String.valueOf(dpi))));
    }

    private ResponseEntity<JobResponse> accepted(JobResponse body) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }
}
