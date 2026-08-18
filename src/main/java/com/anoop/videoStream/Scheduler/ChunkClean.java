package com.anoop.videoStream.Scheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.anoop.videoStream.queue.StreamingSessionQueue;
import com.anoop.videoStream.util.FileOperation;

@Component
public class ChunkClean {

    @Value("${video.storage.path}")
    private String videoStoragePath;

    private StreamingSessionQueue streamingSessionQueue;
    private FileOperation fileOperation;

    public ChunkClean(StreamingSessionQueue streamingSessionQueue, FileOperation fileOperation) {
        this.streamingSessionQueue = streamingSessionQueue;
        this.fileOperation = fileOperation;
    }

    @Scheduled(cron = "0 * * * * *")
    public void cleanup() {

        List<String> streamSession = streamingSessionQueue.getStreamSession();

        for (String videoId : streamSession) {
            Path path = Path.of(videoStoragePath, videoId);
            try {
                fileOperation.deleteDirectory(path);
                if (!Files.exists(path)) {
                    streamingSessionQueue.removeExpierSession(videoId);
                }

            } catch (IOException e) {
                System.out.println("This Exception come from Chunk Cleanup Scheduler");
                e.printStackTrace();
            }

        }

    }
}
