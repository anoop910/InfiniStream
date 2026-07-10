package com.anoop.videoStream.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class VideoChunkUploadRequest {

    private MultipartFile chunk;

    private int chunkIndex;

    private String fileName;

    private String videoID;

    private Boolean lastChunk;

    private int totalChunk;
}