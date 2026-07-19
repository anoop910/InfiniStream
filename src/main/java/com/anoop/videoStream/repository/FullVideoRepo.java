package com.anoop.videoStream.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.dto.GetMyVideoResponse;



public interface FullVideoRepo extends JpaRepository<FullVideo, Long>{
     Optional<FullVideo> findByVideoID(String videoID);


     @Query("""
            SELECT new com.anoop.videoStream.dto.GetMyVideoResponse(
                f.fileName,
                f.videoID,
                f.width,
                f.height,
                f.duration
            )
            FROM FullVideo f
            """)
    List<GetMyVideoResponse> getMyVideos();
}
