package com.anoop.videoStream.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;

@Configuration
public class ExecutorConfig {

    // @Value("${upload.worker.threads:3}")
    // private int workerThread;

    // private ExecutorService executor = Executors.newFixedThreadPool(3);

    // @Autowired
    // private VideoUploadToTelegram videoUploadToTelegram;

   

    // @Autowired
    // private VideoChunkQueue videoChunkQueue;

    @Bean
    public ExecutorService executor() {

    return Executors.newFixedThreadPool(4);
    }

    @Bean
    public ExecutorService retryExecutor(){
        return Executors.newSingleThreadExecutor();
    }

    // @PostConstruct
    // public void createWoker() {
    //     for (int i = 0; i < workerThread; i++) {
    //         executor.submit(this::workerLoop);

    //     }

    // }

    // private void workerLoop() {

    //     while (true) {
    //         try {

    //             ChunkUploadTask taskToQueue = videoChunkQueue.getTaskToQueue();
    //             videoUploadToTelegram.uploadToTelegram(taskToQueue);

    //         } catch (Exception e) {
    //            e.printStackTrace();
    //         }

    //     }
    // }

    @PreDestroy
    public void shutdown() {

        System.out.println("Shutting down upload workers...");

        executor().shutdown();
        retryExecutor().shutdown();

        try {

            if (!executor().awaitTermination(30, TimeUnit.SECONDS)|| !retryExecutor().awaitTermination(30, TimeUnit.SECONDS)) {

                System.out.println("Force shutdown");

                executor().shutdownNow();
                retryExecutor().shutdownNow();
            }

        } catch (InterruptedException e) {

            executor().shutdownNow();
            retryExecutor().shutdownNow();

            Thread.currentThread().interrupt();
        }
    }
}
