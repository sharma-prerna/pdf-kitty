package com.pdftoolkit.controller;

import com.pdftoolkit.service.DownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;

import java.util.UUID;

@Tag(name = "Download", description = "Download generated outputs")
@RestController
@RequestMapping("/api/download")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    @Operation(summary = "Download a completed job's output (single file or ZIP archive)")
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        DownloadService.DownloadPayload payload = downloadService.resolve(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(payload.fileName())
                .build();
        MediaType mediaType = payload.contentType() != null
                ? MediaType.parseMediaType(payload.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(mediaType)
                .contentLength(payload.size())
                .body(payload.resource());
    }
}
