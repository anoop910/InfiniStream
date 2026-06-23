package com.anoop.videoStream.telegramService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.ChunkUploadTask;
import com.anoop.videoStream.queue.RetryQueue;

@Component
public class HandleUploadFailure {
    @Autowired
    private RetryQueue retryQueue;


    public void handleUploadFailure(ChunkUploadTask task){
            try {
                retryQueue.setRetryTask(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
