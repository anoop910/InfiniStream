package com.anoop.videoStream.dto;

import jakarta.persistence.Column;

public class Document {

    private String file_id;

    private String file_unique_id;

    private long file_size;

    private String telegramFilePath;

    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public String getFile_unique_id() {
        return file_unique_id;
    }

    public void setFile_unique_id(String file_unique_id) {
        this.file_unique_id = file_unique_id;
    }

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public String getTelegramFilePath() {
        return telegramFilePath;
    }

    public void setTelegramFilePath(String telegramFilePath) {
        this.telegramFilePath = telegramFilePath;
    }
}