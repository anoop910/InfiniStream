package com.anoop.videoStream.memory;

import java.util.List;
import com.anoop.videoStream.Model.VideoChunk;
import lombok.Data;

@Data
public class StreamSession {

    private long totalSize;
    private String fileName;
    private Double duration;
   private List<VideoChunk> videoChunks;
}
