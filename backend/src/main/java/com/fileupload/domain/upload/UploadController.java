package com.fileupload.domain.upload;

import com.fileupload.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity<ApiResponse<UploadService.UploadResult>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        log.info("upload request: name={}, size={}, empty={}", file.getOriginalFilename(), file.getSize(), file.isEmpty());

        UploadService.UploadResult result = uploadService.upload(file);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
