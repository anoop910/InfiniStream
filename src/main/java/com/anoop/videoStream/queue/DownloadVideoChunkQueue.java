package com.anoop.videoStream.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.DownloadVideoTask;

@Component
public class DownloadVideoChunkQueue {
    private BlockingQueue<DownloadVideoTask> tasks = new ArrayBlockingQueue<>(20);

    public String setTaskToQueue(DownloadVideoTask downloadVideoTask) throws InterruptedException {
        tasks.put(downloadVideoTask);
        return "video chunk download  task added";
      
    }

    public DownloadVideoTask getTaskToQueue() throws InterruptedException {
        return tasks.take();
    }
}
