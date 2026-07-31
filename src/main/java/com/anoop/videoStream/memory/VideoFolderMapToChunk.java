package com.anoop.videoStream.memory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class VideoFolderMapToChunk {
    

    ConcurrentHashMap<String, Path> videoFolder = new ConcurrentHashMap<>();


    public void setVideoFolderPath(String videoId, Path path){
        videoFolder.put(videoId, path);
    }

    public Path getVideoFolderPath(String videoId){
        return videoFolder.get(videoId);
    }

    public void removeVideoFolderPath(String videoId){
        videoFolder.remove(videoId);
    }
}
