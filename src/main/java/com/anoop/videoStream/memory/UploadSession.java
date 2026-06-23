package com.anoop.videoStream.memory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.Model.VideoChunk;

import lombok.Data;

@Data
public class UploadSession {

    private String videoId;

    private String fileName;

    private int totalChunks;

    private FullVideo video;

    private AtomicInteger uploadedChunks =
            new AtomicInteger(0);

    private ConcurrentHashMap<Integer, VideoChunk> chunks =
            new ConcurrentHashMap<>();

}