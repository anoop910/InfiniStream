package com.anoop.videoStream.service;

import java.util.List;

import com.anoop.videoStream.dto.GetMyVideoResponse;

public interface VideoMetadataService {
    public List<GetMyVideoResponse> getMyVideo();
}
