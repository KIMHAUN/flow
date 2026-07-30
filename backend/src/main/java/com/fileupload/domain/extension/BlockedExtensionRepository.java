package com.fileupload.domain.extension;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BlockedExtensionRepository extends JpaRepository<BlockedExtension, Long> {

    List<BlockedExtension> findAllByType(BlockedExtension.ExtensionType type);

    Optional<BlockedExtension> findByExtension(String extension);

    boolean existsByExtension(String extension);

    // 비관적 잠금으로 카운트 — 동시 요청 시 200개 초과 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(b) FROM BlockedExtension b WHERE b.type = 'CUSTOM'")
    long countCustomForUpdate();

    // 실제 차단 중인 확장자 목록 조회 (업로드 검사용)
    @Query("SELECT b.extension FROM BlockedExtension b WHERE b.type = 'CUSTOM' OR (b.type = 'FIXED' AND b.isBlocked = true)")
    Set<String> findAllBlockedExtensions();
}
