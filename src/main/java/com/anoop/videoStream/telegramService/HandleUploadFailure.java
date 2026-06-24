package com.anoop.videoStream.telegramService;

import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.ChunkUploadTask;
import com.anoop.videoStream.queue.RetryQueue;


@Component

public class HandleUploadFailure {
    private RetryQueue retryQueue;


    public HandleUploadFailure(RetryQueue retryQueue) {
        this.retryQueue = retryQueue;
    }


    public void handleUploadFailure(ChunkUploadTask task){
            try {
                retryQueue.setRetryTask(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
