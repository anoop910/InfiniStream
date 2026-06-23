package com.anoop.videoStream.dto;

import lombok.Data;

@Data
public class VideoChunkKey {
    private String videoID;
    private int chunkIndex;
}
