package com.fileupload.domain.extension;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockedExtensionService {

    private static final int CUSTOM_MAX_COUNT = 200;

    private final BlockedExtensionRepository repository;

    public List<BlockedExtension> getFixedExtensions() {
        return repository.findAllByType(BlockedExtension.ExtensionType.FIXED);
    }

    public List<BlockedExtension> getCustomExtensions() {
        return repository.findAllByType(BlockedExtension.ExtensionType.CUSTOM);
    }

    @Transactional
    public BlockedExtension updateFixedBlocked(String extension, boolean isBlocked) {
        BlockedExtension entity = repository.findByExtension(extension.toLowerCase())
                .filter(e -> e.getType() == BlockedExtension.ExtensionType.FIXED)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고정 확장자입니다: " + extension));
        entity.updateBlocked(isBlocked);
        return entity;
    }

    @Transactional
    public BlockedExtension addCustomExtension(String extension) {
        String normalized = normalize(extension);

        // 비관적 잠금으로 카운트 조회 — count + save 사이 동시 요청이 끼어들지 못하게 막음
        long customCount = repository.countCustomForUpdate();
        if (customCount >= CUSTOM_MAX_COUNT) {
            throw new IllegalArgumentException("커스텀 확장자는 최대 " + CUSTOM_MAX_COUNT + "개까지 등록 가능합니다.");
        }

        if (repository.existsByExtension(normalized)) {
            throw new IllegalArgumentException("이미 등록된 확장자입니다: " + normalized);
        }

        return repository.save(BlockedExtension.createCustom(normalized));
    }

    @Transactional
    public void deleteCustomExtension(String extension) {
        BlockedExtension entity = repository.findByExtension(extension.toLowerCase())
                .filter(e -> e.getType() == BlockedExtension.ExtensionType.CUSTOM)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커스텀 확장자입니다: " + extension));
        repository.delete(entity);
    }

    public Set<String> getBlockedExtensionSet() {
        return repository.findAllBlockedExtensions();
    }

    private String normalize(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("확장자를 입력해주세요.");
        }
        // 앞뒤 공백 제거, 소문자 변환, 앞의 점(.) 제거
        String normalized = extension.strip().toLowerCase().replaceAll("^\\.", "");

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 확장자입니다.");
        }
        if (normalized.length() > 20) {
            throw new IllegalArgumentException("확장자는 최대 20자까지 입력 가능합니다.");
        }
        // 영문자, 숫자만 허용 (특수문자, 공백, 유니코드 차단)
        if (!normalized.matches("[a-z0-9]+")) {
            throw new IllegalArgumentException("확장자는 영문자와 숫자만 사용 가능합니다.");
        }
        return normalized;
    }
}
