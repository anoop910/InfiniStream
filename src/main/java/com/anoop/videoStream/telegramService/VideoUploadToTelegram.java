package com.anoop.videoStream.telegramService;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;

import com.anoop.videoStream.Model.ChunkUploadTask;
import com.anoop.videoStream.Model.VideoChunk;
import com.anoop.videoStream.config.TelegramWebClientConfig;
import com.anoop.videoStream.dto.TelegramResponse;
import com.anoop.videoStream.memory.UploadSession;
import com.anoop.videoStream.memory.UploadSessionManager;
import com.anoop.videoStream.queue.RetryQueue;

import reactor.util.retry.Retry;

@Component
public class VideoUploadToTelegram {

        @Autowired
        private TelegramWebClientConfig telegramWebClientConfig;


        @Autowired
        private UploadSessionManager sessionManager;

        @Autowired
        private DatabaseFlushService databaseFlushService;

        @Autowired
        private RetryQueue retryQueue;

        @Value("${telegram.bot.token}")
        private String botToken;

        @Value("${telegram.chat.id}")
        private String chatId;

        // @Autowired
        // private UploadService uploadService;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        public void uploadToTelegram(ChunkUploadTask task) {
                System.out.println(
                                "UPLOADING..." + LocalTime.now().format(formatter) + " " + task.getFileName() + " "
                                                + task.getChunkIndex());
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("chat_id", chatId);
                builder.part("document", new ByteArrayResource(task.getData()) {
                        @Override
                        public String getFilename() {
                                return task.getFileName() + "." + task.getChunkIndex();
                        }
                });

                TelegramResponse response = telegramWebClientConfig.getWebClient()
                                .post()
                                .uri("/bot" + botToken + "/sendDocument")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .body(BodyInserters.fromMultipartData(builder.build()))
                                .retrieve()
                                .bodyToMono(TelegramResponse.class)
                                .timeout(Duration.ofSeconds(60))
                                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(1)))
                                .doOnError(error -> {
                                        try {
                                                retryQueue.setRetryTask(task);
                                        } catch (InterruptedException e) {
                                                e.printStackTrace();
                                        }
                                })
                                .block();


                System.out.println("UPLOAD DONE " + LocalTime.now().format(formatter) + " " + task.getFileName() + " "
                                                + task.getChunkIndex());
                System.out.println("UPLOADED " +
                                                LocalTime.now().format(formatter) + " " +
                                                response.isOk() + " " +
                                                response.getResult().getDocument().getFile_id() + " " +
                                                response.getResult().getDocument().getFile_unique_id());

                if (response.isOk()) {

                        UploadSession session = sessionManager.getOrCreateSession(task.getVideoID(), task.getFileName(), task.getTotalChunk());

                        VideoChunk chunk = new VideoChunk();

                        chunk.setChunkIndex(task.getChunkIndex());

                        chunk.setTelegramFileId(response.getResult().getDocument().getFile_id());

                        chunk.setTelegramUniqueId(response.getResult().getDocument().getFile_unique_id());

                        chunk.setRetryCount(task.getRetryCount());

                        chunk.setVideoID(task.getVideoID());
                        chunk.setFullVideo(session.getVideo());
                        

                        session.getChunks().put(task.getChunkIndex(),chunk);

                        int uploaded = session.getUploadedChunks().incrementAndGet();

                        System.out.println("Video : " + task.getVideoID()
                                                        + " Uploaded : "
                                                        + uploaded
                                                        + "/"
                                                        + session.getTotalChunks());

                        if (uploaded == session.getTotalChunks()) {

                        databaseFlushService
                        .flushToDatabase(session);

                        sessionManager.removeSession(
                        session.getVideoId());
                        }
                }
        }

}
