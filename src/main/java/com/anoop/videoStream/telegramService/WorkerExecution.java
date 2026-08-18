package com.anoop.videoStream.telegramService;

import com.anoop.videoStream.stream.streamService.DownloadVideoChunk;
import org.springframework.stereotype.Component;

import com.anoop.videoStream.Model.ChunkUploadTask;
import com.anoop.videoStream.Model.DownloadVideoTask;
import com.anoop.videoStream.config.ExecutorConfig;
import com.anoop.videoStream.queue.DownloadVideoChunkQueue;
import com.anoop.videoStream.queue.RetryQueue;
import com.anoop.videoStream.queue.VideoChunkQueue;

import jakarta.annotation.PostConstruct;

@Component
public class WorkerExecution {

    private final DownloadVideoChunk downloadVideoChunk;

    private final DownloadVideoChunkQueue downloadVideoChunkQueue;

    private VideoUploadToTelegram videoUploadToTelegram;

    private VideoChunkQueue videoChunkQueue;

    private ExecutorConfig executorConfig;

    private RetryQueue retryQueue;

    public WorkerExecution(VideoUploadToTelegram videoUploadToTelegram, VideoChunkQueue videoChunkQueue,
            ExecutorConfig executorConfig, RetryQueue retryQueue, DownloadVideoChunk downloadVideoChunk,
            DownloadVideoChunkQueue downloadVideoChunkQueue) {
        this.videoUploadToTelegram = videoUploadToTelegram;
        this.videoChunkQueue = videoChunkQueue;
        this.executorConfig = executorConfig;
        this.retryQueue = retryQueue;
        this.downloadVideoChunk = downloadVideoChunk;
        this.downloadVideoChunkQueue = downloadVideoChunkQueue;
    }

    @PostConstruct
    public void createWoker() {
        for (int i = 0; i < 3; i++) {
            executorConfig.executor().submit(this::uploadWorkerLoop);

        }

        for (int i = 0; i < 2; i++) {
            executorConfig.executor().submit(this::downloadWokerLoop);
        }

    }

    private void downloadWokerLoop() {
        while (true) {

            try {
                DownloadVideoTask taskToQueue = downloadVideoChunkQueue.getTaskToQueue();
                String videoId = taskToQueue.getVideoId();
                int index = taskToQueue.getIndex();

                

                downloadVideoChunk.downloadChunkByIndex(videoId, index);
                downloadVideoChunkQueue.removeSetQueue(taskToQueue);
            } catch (Exception e) {
               e.printStackTrace();
            }

        }
    }

    private void uploadWorkerLoop() {

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

            while (true) {

                try {

                    ChunkUploadTask uploadTask = retryQueue.getUploadTask();

                    uploadTask.incrementRetry();

                    videoUploadToTelegram
                            .uploadToTelegram(uploadTask);

                } catch (Exception e) {

                    System.out.println(
                            "Retry failed");
                }
            }
        });
    }

}
