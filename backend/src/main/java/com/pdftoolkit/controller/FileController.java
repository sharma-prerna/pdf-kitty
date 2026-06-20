package com.pdftoolkit.controller;

import com.pdftoolkit.dto.FileMetadataResponse;
import com.pdftoolkit.service.PdfOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Files", description = "Upload and validate files")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final PdfOperationService operationService;

    @Operation(summary = "Validate one or more files (size, extension, content type)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<FileMetadataResponse> upload(@RequestParam("files") List<MultipartFile> files) {
        return operationService.validateOnly(files);
    }
}
