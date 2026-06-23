package com.anoop.videoStream.memory;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.repository.FullVideoRepo;

@Service
public class StreamSessionManager {
    

    @Autowired
    private FullVideoRepo fullVideoRepo;

    ConcurrentHashMap<String, StreamSession> activeStream = new ConcurrentHashMap<>();

    public StreamSession getOrCreateStreamSession(String vidoeID){
        return activeStream.computeIfAbsent(vidoeID, id ->{
            StreamSession session = new StreamSession();

            FullVideo byVideoID = fullVideoRepo.findByVideoID(vidoeID).orElseThrow(()-> new RuntimeException("Video not exist with this Video ID" + vidoeID));
            session.setDuration(byVideoID.getDuration());
            session.setFileName(byVideoID.getFileName());
            session.setTotalSize(byVideoID.getTotalSize());
            session.setVideoChunks(byVideoID.getVideoChunks());
            return session;

        });
    }

     public StreamSession getSession(String videoId) {
                return activeStream.get(videoId);
        }

        public void removeSession(String videoId) {
                activeStream.remove(videoId);
        }

}
