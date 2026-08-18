package com.anoop.videoStream.controller;

import com.anoop.videoStream.telegramService.DatabaseFlushService;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anoop.videoStream.dto.GetMyVideoResponse;
import com.anoop.videoStream.service.VideoMetadataService;

@RestController
@CrossOrigin(origins = "*")
public class VideoMetadataController {
    
    private VideoMetadataService videoMetadataService;

    public VideoMetadataController(VideoMetadataService videoMetadataService) {
        this.videoMetadataService = videoMetadataService;
      
    }

    @GetMapping("/get/myvideo")
    public List<GetMyVideoResponse> getMyAllVideos(Authentication authentication){
        String user = authentication.getName();
        Long userId = Long.parseLong(user);
        System.out.println(userId);
       
        return videoMetadataService.getMyVideo(userId);
    }

    
}
