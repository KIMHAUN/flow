package com.fileupload.common.exception;

public class BlockedFileException extends RuntimeException {

    private final String extension;

    public BlockedFileException(String extension) {
        super(extension + " 확장자는 업로드가 차단되어 있습니다.");
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
