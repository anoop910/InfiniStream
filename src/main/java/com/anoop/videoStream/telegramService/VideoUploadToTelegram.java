package com.anoop.videoStream.telegramService;

import com.anoop.videoStream.memory.VideoFolderMapToChunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
import com.anoop.videoStream.util.FileOperation;

import reactor.util.retry.Retry;

import tools.jackson.databind.JsonNode;

@Component
public class VideoUploadToTelegram {

        private final VideoFolderMapToChunk videoFolderMapToChunk;
        private final TelegramWebClientConfig telegramWebClientConfig;
        private final UploadSessionManager sessionManager;
        private final DatabaseFlushService databaseFlushService;
        private final RetryQueue retryQueue;
        private final FileOperation fileOperation;

        @Value("${telegram.bot.token}")
        private String botToken;

        @Value("${telegram.chat.id}")
        private String chatId;

        @Value("${telegram.upload.retry}")
        private int uploadRetry;

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        public VideoUploadToTelegram(
                        TelegramWebClientConfig telegramWebClientConfig,
                        UploadSessionManager sessionManager,
                        DatabaseFlushService databaseFlushService,
                        RetryQueue retryQueue,
                        VideoFolderMapToChunk videoFolderMapToChunk,
                        FileOperation fileOperation) {

                this.telegramWebClientConfig = telegramWebClientConfig;

                this.sessionManager = sessionManager;

                this.databaseFlushService = databaseFlushService;

                this.retryQueue = retryQueue;

                this.videoFolderMapToChunk = videoFolderMapToChunk;

                this.fileOperation = fileOperation;
        }

        /*
         * ============================================================
         * MAIN UPLOAD METHOD
         * ============================================================
         */

        public void uploadToTelegram(
                        ChunkUploadTask task)
                        throws IOException {

                System.out.println(
                                "UPLOADING..."
                                                + LocalTime.now().format(formatter)
                                                + " "
                                                + task.getFileName()
                                                + " "
                                                + task.getChunkIndex());

                LocalDateTime uploadStartedAt = LocalDateTime.now();

                Path path = task.getPath();

                /*
                 * ========================================================
                 * CURRENT LOGIC
                 * ========================================================
                 *
                 * You are using readAllBytes().
                 *
                 * For your current 1 MB chunks this is acceptable,
                 * although later we can optimize this further to
                 * avoid loading the whole chunk into a byte[].
                 */

                byte[] allBytes = Files.readAllBytes(path);

                MultipartBodyBuilder builder = new MultipartBodyBuilder();

                builder.part(
                                "chat_id",
                                chatId);

                builder.part(
                                "document",
                                new ByteArrayResource(allBytes) {

                                        @Override
                                        public String getFilename() {

                                                return task.getFileName()
                                                                + "."
                                                                + task.getChunkIndex();
                                        }
                                });

                /*
                 * ========================================================
                 * TELEGRAM sendDocument
                 * ========================================================
                 */

                TelegramResponse response = telegramWebClientConfig
                                .getWebClient()
                                .post()
                                .uri(
                                                "/bot"
                                                                + botToken
                                                                + "/sendDocument")
                                .contentType(
                                                MediaType.MULTIPART_FORM_DATA)
                                .body(
                                                BodyInserters
                                                                .fromMultipartData(
                                                                                builder.build()))
                                .retrieve()
                                .bodyToMono(
                                                TelegramResponse.class)
                                .timeout(
                                                Duration.ofSeconds(60))
                                .retryWhen(
                                                Retry.fixedDelay(
                                                                uploadRetry,
                                                                Duration.ofSeconds(1)))
                                .doOnError(error -> {

                                        try {

                                                retryQueue.setRetryTask(
                                                                task);

                                        } catch (InterruptedException e) {

                                                Thread.currentThread()
                                                                .interrupt();

                                                e.printStackTrace();
                                        }
                                })
                                .block();

                LocalDateTime uploadCompletedAt = LocalDateTime.now();

                /*
                 * ========================================================
                 * CHECK TELEGRAM RESPONSE
                 * ========================================================
                 */

                if (response == null ||
                                !response.isOk() ||
                                response.getResult() == null ||
                                response.getResult().getDocument() == null) {

                        throw new IOException(
                                        "Telegram upload failed for chunk "
                                                        + task.getChunkIndex());
                }

                /*
                 * ========================================================
                 * GET TELEGRAM FILE ID
                 * ========================================================
                 */

                String telegramFileId = response
                                .getResult()
                                .getDocument()
                                .getFile_id();

                String telegramUniqueId = response
                                .getResult()
                                .getDocument()
                                .getFile_unique_id();

                System.out.println(
                                "UPLOAD DONE "
                                                + LocalTime.now().format(formatter)
                                                + " "
                                                + task.getFileName()
                                                + " "
                                                + task.getChunkIndex());

                System.out.println(
                                "TELEGRAM FILE ID : "
                                                + telegramFileId);

                System.out.println(
                                "TELEGRAM UNIQUE ID : "
                                                + telegramUniqueId);

                /*
                 * ========================================================
                 * NEW:
                 *
                 * GET file_path NOW
                 *
                 * This happens once at upload time.
                 * It will NOT be required during normal streaming.
                 * ========================================================
                 */

                String telegramFilePath = getTelegramFilePath(
                                telegramFileId);

                System.out.println(
                                "TELEGRAM FILE PATH : "
                                                + telegramFilePath);

                /*
                 * ========================================================
                 * CREATE UPLOAD SESSION
                 * ========================================================
                 */

                UploadSession session = sessionManager.getOrCreateSession(
                                task.getVideoID(),
                                task.getFileName(),
                                task.getTotalChunk());

                /*
                 * ========================================================
                 * CREATE VideoChunk
                 * ========================================================
                 */

                VideoChunk chunk = new VideoChunk();

                chunk.setChunkIndex(
                                task.getChunkIndex());

                /*
                 * Telegram information
                 */

                chunk.setTelegramFileId(
                                telegramFileId);

                chunk.setTelegramUniqueId(
                                telegramUniqueId);

                /*
                 * NEW
                 *
                 * Save file_path.
                 */

                chunk.setTelegramFilePath(
                                telegramFilePath);

                /*
                 * Existing information
                 */

                chunk.setRetryCount(
                                task.getRetryCount());

                chunk.setUploadStartedAt(
                                uploadStartedAt);

                chunk.setUploadCompletedAt(
                                uploadCompletedAt);

                chunk.setVideoID(
                                task.getVideoID());

                chunk.setFullVideo(
                                session.getVideo());

                /*
                 * ========================================================
                 * ADD TO MEMORY
                 * ========================================================
                 */

                session.getChunks().put(
                                task.getChunkIndex(),
                                chunk);

                int uploaded = session
                                .getUploadedChunks()
                                .incrementAndGet();

                System.out.println(
                                "Video : "
                                                + task.getVideoID()
                                                + " Uploaded : "
                                                + uploaded
                                                + "/"
                                                + session.getTotalChunks());

                /*
                 * ========================================================
                 * ALL CHUNKS UPLOADED
                 * ========================================================
                 */

                if (uploaded == session.getTotalChunks()) {

                        /*
                         * Persist all VideoChunk objects.
                         *
                         * telegramFilePath is included.
                         */

                        databaseFlushService
                                        .flushToDatabase(
                                                        session);

                        /*
                         * Remove upload session.
                         */

                        sessionManager.removeSession(
                                        session.getVideoId());

                        /*
                         * Delete temporary upload chunks.
                         */

                        fileOperation.deleteDirectory(
                                        videoFolderMapToChunk
                                                        .getVideoFolderPath(
                                                                        session.getVideoId()));

                        /*
                         * Remove folder mapping.
                         */

                        videoFolderMapToChunk
                                        .removeVideoFolderPath(
                                                        session.getVideoId());
                }
        }

        /*
         * ============================================================
         * TELEGRAM getFile()
         * ============================================================
         *
         * Called ONLY after upload.
         *
         * sendDocument()
         * ↓
         * file_id
         * ↓
         * getFile()
         * ↓
         * file_path
         *
         * file_path is then stored in VideoChunk.
         *
         * ============================================================
         */

        private String getTelegramFilePath(
                        String fileId) {

                System.out.println(
                                "GET FILE PATH START : "
                                                + LocalTime.now().format(formatter));

                JsonNode root = telegramWebClientConfig
                                .getWebClient()
                                .get()
                                .uri(
                                                uriBuilder -> uriBuilder
                                                                .path(
                                                                                "/bot{token}/getFile")
                                                                .queryParam(
                                                                                "file_id",
                                                                                fileId)
                                                                .build(
                                                                                botToken))
                                .retrieve()
                                .bodyToMono(
                                                JsonNode.class)
                                .timeout(
                                                Duration.ofSeconds(15))
                                .block();

                System.out.println(
                                "GET FILE PATH END : "
                                                + LocalTime.now().format(formatter));

                /*
                 * Validate response.
                 */

                if (root == null ||
                                !root.path("ok")
                                                .asBoolean(false)) {

                        throw new RuntimeException(
                                        "Failed to get Telegram file path "
                                                        + "for fileId="
                                                        + fileId);
                }

                String filePath = root.path("result")
                                .path("file_path")
                                .asString(null);

                if (filePath == null ||
                                filePath.isBlank()) {

                        throw new RuntimeException(
                                        "Telegram file_path is missing "
                                                        + "for fileId="
                                                        + fileId);
                }

                return filePath;
        }
}