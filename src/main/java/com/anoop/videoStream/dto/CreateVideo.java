package com.anoop.videoStream.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.anoop.videoStream.Model.VideoChunk;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
public class CreateVideo {

   
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private String fileName;
   private Double width;
   private Double height;
   private Double duration;
   private int totalChunk;
   private String videoID;
   private int uploadedChunks;

   private long totalSize;

//    @Enumerated(EnumType.STRING)
//    private UploadStatus status;

   private LocalDateTime createdAt = LocalDateTime.now();
   private LocalDateTime completedAt;
   @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "fullVideo")
   @JsonManagedReference
   private List<VideoChunk> videoChunks;
}
