package com.fileupload.domain.extension;

import com.fileupload.common.response.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/extensions")
@RequiredArgsConstructor
@Validated
public class BlockedExtensionController {

    private final BlockedExtensionService service;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAll() {
        List<BlockedExtension> fixed = service.getFixedExtensions();
        List<BlockedExtension> custom = service.getCustomExtensions();

        List<Map<String, Object>> fixedResponse = fixed.stream()
                .map(e -> Map.<String, Object>of("extension", e.getExtension(), "isBlocked", e.isBlocked()))
                .toList();

        List<String> customResponse = custom.stream()
                .map(BlockedExtension::getExtension)
                .toList();

        Map<String, Object> body = Map.of(
                "fixed", fixedResponse,
                "custom", customResponse,
                "customCount", customResponse.size()
        );
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @PatchMapping("/fixed/{extension}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateFixed(
            @PathVariable String extension,
            @RequestBody Map<String, Boolean> body) {

        Boolean isBlocked = body.get("isBlocked");
        if (isBlocked == null) {
            throw new IllegalArgumentException("isBlocked 값이 필요합니다.");
        }

        BlockedExtension updated = service.updateFixedBlocked(extension, isBlocked);
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("extension", updated.getExtension(), "isBlocked", updated.isBlocked())
        ));
    }

    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<Map<String, String>>> addCustom(
            @RequestBody @Validated CustomExtensionRequest request) {

        BlockedExtension saved = service.addCustomExtension(request.extension());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("extension", saved.getExtension())));
    }

    @DeleteMapping("/custom/{extension}")
    public ResponseEntity<ApiResponse<Void>> deleteCustom(@PathVariable String extension) {
        service.deleteCustomExtension(extension);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    record CustomExtensionRequest(
            @NotBlank(message = "확장자를 입력해주세요.")
            @Size(max = 20, message = "확장자는 최대 20자까지 입력 가능합니다.")
            String extension
    ) {}
}
