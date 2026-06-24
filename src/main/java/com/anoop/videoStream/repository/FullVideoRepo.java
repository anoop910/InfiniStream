package com.anoop.videoStream.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anoop.videoStream.Model.FullVideo;



public interface FullVideoRepo extends JpaRepository<FullVideo, Long>{
     Optional<FullVideo> findByVideoID(String videoID);
}
