package com.anoop.videoStream.queue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.ChunkUploadTask;

@Component
public class VideoChunkQueue {
    // @Value("${queue.capacity}")
    // private int queueCapacity;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private BlockingQueue<ChunkUploadTask> tasks = new ArrayBlockingQueue<>(6);

    public String setTaskToQueue(ChunkUploadTask chunkUploadTask) throws InterruptedException {
        tasks.put(chunkUploadTask);
        return "TASK ADDED..." + LocalTime.now().format(formatter) + chunkUploadTask.getFileName() + " "
                + chunkUploadTask.getChunkIndex() + " " + Thread.currentThread().getName();
    }

    public ChunkUploadTask getTaskToQueue() throws InterruptedException {
        return tasks.take();
    }

}
