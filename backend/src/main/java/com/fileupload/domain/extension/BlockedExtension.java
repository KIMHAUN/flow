package com.fileupload.domain.extension;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_extension")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String extension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExtensionType type;

    @Column(nullable = false)
    private boolean isBlocked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ExtensionType {
        FIXED, CUSTOM
    }

    // FIXED 확장자 생성 (초기 데이터용)
    public static BlockedExtension createFixed(String extension) {
        BlockedExtension e = new BlockedExtension();
        e.extension = extension.toLowerCase();
        e.type = ExtensionType.FIXED;
        e.isBlocked = false;
        e.createdAt = LocalDateTime.now();
        return e;
    }

    // CUSTOM 확장자 생성
    public static BlockedExtension createCustom(String extension) {
        BlockedExtension e = new BlockedExtension();
        e.extension = extension.toLowerCase();
        e.type = ExtensionType.CUSTOM;
        e.isBlocked = true;
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public void updateBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}
