package com.anoop.videoStream.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VideoChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int chunkIndex;

    private String telegramFileId;

    private String telegramUniqueId;
    
    private String telegramFilePath;

    private String videoID;

    private String botId;

    private int retryCount;

    private long fileSize;

    private String checksum;

    // @Enumerated(EnumType.STRING)
    // private ChunkStatus status;

    private LocalDateTime uploadStartedAt;

    private LocalDateTime uploadCompletedAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "video_id")
    private FullVideo fullVideo;

}
