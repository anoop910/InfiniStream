package com.anoop.videoStream.dto;

import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;

@Data
public class CreateVideo {
   private String fileName;
   private Double videoDuration;
   private String videoID;
   private Double height;
   private Double width;
   private int totalChunk;
   private Long totalSize;
}


