package com.anoop.videoStream.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anoop.videoStream.Model.VideoChunk;

public interface VideoChunkRepo extends JpaRepository<VideoChunk, Long>{
    
}
