package com.anoop.videoStream.memory;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.repository.FullVideoRepo;


@Service
public class UploadSessionManager {

        
        private FullVideoRepo fullVideoRepo;
        

        public UploadSessionManager(FullVideoRepo fullVideoRepo) {
                this.fullVideoRepo = fullVideoRepo;
        }

        private ConcurrentHashMap<String, UploadSession> activeUploads = new ConcurrentHashMap<>();

        public UploadSession getOrCreateSession(String videoId, String fileName, int totalChunks) {
                return activeUploads.computeIfAbsent(videoId, id -> {

                        UploadSession session = new UploadSession();
                        FullVideo byVideoID = fullVideoRepo.findByVideoID(videoId).orElseThrow(() -> new RuntimeException("Video not by :" + videoId));
                        session.setVideoId(videoId);
                        session.setFileName(fileName);
                        session.setTotalChunks(totalChunks);
                        session.setVideo(byVideoID);

                        return session;
                });
        }

        public UploadSession getSession(String videoId) {
                return activeUploads.get(videoId);
        }

        public void removeSession(String videoId) {
                activeUploads.remove(videoId);
        }
}