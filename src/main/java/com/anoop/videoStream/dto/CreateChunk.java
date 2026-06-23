package com.anoop.videoStream.dto;

import java.time.LocalDateTime;

import com.anoop.videoStream.Model.FullVideo;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class CreateChunk {
    
 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int chunkIndex;

    private String telegramFileId;

    private String telegramUniqueId;

    private String videoID;

    private String botId;

    private int retryCount;

    private long fileSize;

    private String checksum;

    // @Enumerated(EnumType.STRING)
    // private ChunkStatus status;

    private LocalDateTime uploadStartedAt;

    private LocalDateTime uploadCompletedAt;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private FullVideo fullVideo;
}
