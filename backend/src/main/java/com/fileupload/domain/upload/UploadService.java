package com.fileupload.domain.upload;

import com.fileupload.common.exception.BlockedFileException;
import com.fileupload.domain.extension.BlockedExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final BlockedExtensionService extensionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UploadResult upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);

        validateExtension(extension);  // 차단 확장자 체크 먼저

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일 내용이 비어있습니다.");
        }

        // UUID로 rename하여 저장 (원본 파일명 사용 금지 - 경로 조작 방지)
        String savedName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        file.transferTo(uploadPath.resolve(savedName));

        return new UploadResult(savedName, originalName, file.getSize());
    }

    private void validateExtension(String extension) {
        if (extension.isEmpty()) {
            // 확장자 없는 파일: 현재 정책상 허용 (CONSIDERATIONS.md에 판단 근거 기술)
            return;
        }
        Set<String> blocked = extensionService.getBlockedExtensionSet();
        if (blocked.contains(extension)) {
            throw new BlockedFileException(extension);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        // 이중 확장자 처리: 마지막 확장자만 검사 (file.exe.txt → txt)
        // 보안 관점에서 전체 파일명도 검사하는 방식이 더 안전하나,
        // 현재는 마지막 확장자 기준으로 처리 (CONSIDERATIONS.md에 판단 근거 기술)
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    public record UploadResult(String fileName, String originalName, long size) {}
}
