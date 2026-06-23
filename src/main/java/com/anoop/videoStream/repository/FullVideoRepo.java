package com.anoop.videoStream.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.anoop.videoStream.Model.FullVideo;


@Repository
public interface FullVideoRepo extends JpaRepository<FullVideo, Long>{
     Optional<FullVideo> findByVideoID(String videoID);
}
