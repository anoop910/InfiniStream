package com.anoop.videoStream.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anoop.videoStream.dto.GetMyVideoResponse;
import com.anoop.videoStream.repository.FullVideoRepo;

@Service
public class VideoMetadataServiceImpl implements VideoMetadataService {

    private FullVideoRepo fullVideoRepo;

    

    public VideoMetadataServiceImpl(FullVideoRepo fullVideoRepo) {
        this.fullVideoRepo = fullVideoRepo;
    }



    @Override
    public List<GetMyVideoResponse> getMyVideo() {

        return fullVideoRepo.getMyVideos();
        
    }
    
}
