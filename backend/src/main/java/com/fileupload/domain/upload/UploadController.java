package com.fileupload.domain.upload;

import com.fileupload.common.exception.BlockedFileException;
import com.fileupload.common.response.ApiResponse;
import com.fileupload.domain.extension.BlockedExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final BlockedExtensionService extensionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> upload(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        String extension = extractExtension(filename);

        if (!extension.isEmpty()) {
            Set<String> blocked = extensionService.getBlockedExtensionSet();
            if (blocked.contains(extension)) {
                throw new BlockedFileException(extension);
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
