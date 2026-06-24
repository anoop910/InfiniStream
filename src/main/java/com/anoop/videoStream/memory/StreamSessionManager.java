package com.anoop.videoStream.memory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.repository.FullVideoRepo;

@Service
public class StreamSessionManager {
    

    
    private FullVideoRepo fullVideoRepo;
    

   
    public StreamSessionManager(FullVideoRepo fullVideoRepo) {
        this.fullVideoRepo = fullVideoRepo;
       
    }

    ConcurrentHashMap<String, StreamSession> activeStream = new ConcurrentHashMap<>();
    // @Transactional
    // public StreamSession getOrCreateStreamSession(String vidoeID){
    //     return activeStream.computeIfAbsent(vidoeID, id ->{
    //         StreamSession session = new StreamSession();

    //        System.out.println("================ get video ==================");
    //         FullVideo byVideoID = fullVideoRepo.findByVideoID(vidoeID).orElseThrow(()-> new RuntimeException("Video not exist with this Video ID" + vidoeID));
    //         session.setDuration(byVideoID.getDuration());
    //         session.setFileName(byVideoID.getFileName());
    //         session.setTotalSize(byVideoID.getTotalSize());
    //         session.setVideoChunks(byVideoID.getVideoChunks());
    //         return session;

    //     });
    // }

    @Transactional
public StreamSession getOrCreateStreamSession(
        String videoID) {

    return activeStream.computeIfAbsent(videoID,
            id -> {

    FullVideo video = fullVideoRepo.findByVideoID(id).orElseThrow(() -> new RuntimeException(
                                                "Video not found : " + id));

                StreamSession session = new StreamSession();

                session.setDuration( video.getDuration());

                session.setFileName( video.getFileName());

                session.setTotalSize( video.getTotalSize());

                session.setVideoChunks( new ArrayList<>(video.getVideoChunks()));

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
