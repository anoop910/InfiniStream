package com.anoop.videoStream.queue;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class StreamingSessionQueue {
    ConcurrentHashMap<String, Long> session = new ConcurrentHashMap<>();

    public void setStreamSession(String videoId, Long lastAccessed){
        session.put(videoId, lastAccessed);
    }

    public List<String> getStreamSession(){
    Long now = System.currentTimeMillis();
     List<String> list = session.entrySet().stream()
                             .filter(entry -> now - entry.getValue() > 60_000)
                             .map(entry -> entry.getKey())
                             .toList();
        return list;
    }


    public void removeExpierSession(String videoID){
        session.remove(videoID);
    }
}
