package com.anoop.videoStream.telegramService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.ChunkUploadTask;
import com.anoop.videoStream.config.ExecutorConfig;
import com.anoop.videoStream.queue.RetryQueue;
import com.anoop.videoStream.queue.VideoChunkQueue;

import jakarta.annotation.PostConstruct;

@Component
public class WorkerExecution {

    @Autowired
    private VideoUploadToTelegram videoUploadToTelegram;

    @Autowired
    private VideoChunkQueue videoChunkQueue;

    @Autowired
    private ExecutorConfig executorConfig;

    @Autowired
    private RetryQueue retryQueue;

    @PostConstruct
    public void createWoker() {
        for (int i = 0; i < 3; i++) {
            executorConfig.uploadExecutor().submit(this::workerLoop);

        }

    }

    private void workerLoop() {

        while (true) {
            try {

                ChunkUploadTask taskToQueue = videoChunkQueue.getTaskToQueue();
                videoUploadToTelegram.uploadToTelegram(taskToQueue);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @PostConstruct
public void retryTaskUpload() {

    executorConfig.retryExecutor().submit(() -> {

        while(true) {

            try {

                ChunkUploadTask uploadTask =
                        retryQueue.getUploadTask();

                uploadTask.incrementRetry();

                videoUploadToTelegram
                        .uploadToTelegram(uploadTask);

            } catch (Exception e) {

                System.out.println(
                        "Retry failed"
                );
            }
        }
    });
}

}
