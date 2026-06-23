package com.anoop.videoStream.queue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.ChunkUploadTask;

@Component
public class RetryQueue {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private BlockingQueue<ChunkUploadTask> retryTasks = new ArrayBlockingQueue<>(6);

    public void setRetryTask(ChunkUploadTask task) throws InterruptedException {
        task.incrementRetry();
        retryTasks.put(task);
        System.out.println(
                "FAILDED TASK ADD TO QUEUE " + " " + LocalTime.now().format(formatter) + " " + task.getFileName() + " "
                        + task.getChunkIndex());
    }

    public ChunkUploadTask getUploadTask() throws InterruptedException {

        ChunkUploadTask take = retryTasks.take();
        System.out.println(
                "FAILDED TASK UPLOADING..." + " " + LocalTime.now().format(formatter) + " " + take.getFileName() + " "
                        + take.getChunkIndex());
        return take;
    }

    public Boolean isRetryTaskAvailable(){
        return retryTasks.isEmpty();
    }

}
