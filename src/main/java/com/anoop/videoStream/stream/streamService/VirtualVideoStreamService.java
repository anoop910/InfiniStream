package com.anoop.videoStream.stream.streamService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.DownloadVideoTask;
import com.anoop.videoStream.exception.ChunkNotReadyException;
import com.anoop.videoStream.queue.DownloadVideoChunkQueue;
import com.anoop.videoStream.queue.StreamingSessionQueue;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.OutputStream;

@Service
public class VirtualVideoStreamService {

        @Value("${video.storage.path}")
        private String videoStoragePath;

        private static final long CHUNK_SIZE = 1024 * 1024; // 1 MB

        private static final int BUFFER_SIZE = 64 * 1024; // 64 KB

        // Maximum time to wait for Telegram downloader
        private static final long CHUNK_WAIT_TIMEOUT = 30000;

        // Check every 100 ms
        private static final long CHUNK_CHECK_INTERVAL = 100;

        private final DownloadVideoChunk downloadVideoChunk;

        private final DownloadVideoChunkQueue downloadVideoChunkQueue;

        private final StreamingSessionQueue streamingSessionQueue;

        public VirtualVideoStreamService(
                        DownloadVideoChunk downloadVideoChunk,
                        DownloadVideoChunkQueue downloadVideoChunkQueue,
                        StreamingSessionQueue streamingSessionQueue) {

                this.downloadVideoChunk = downloadVideoChunk;
                this.downloadVideoChunkQueue = downloadVideoChunkQueue;
                this.streamingSessionQueue = streamingSessionQueue;
        }

        public void streamVideo(
                        String videoID,
                        long start,
                        long end,
                        OutputStream outputStream) throws Exception {

                System.out.println("file name : " + videoID);
                System.out.println("start : " + start);
                System.out.println("end : " + end);

                long bytesRemaining = end - start + 1;

                System.out.println(
                                "Remaining bytes : " + bytesRemaining);

                byte[] buffer = new byte[BUFFER_SIZE];

                while (bytesRemaining > 0) {

                        /*
                         * Find chunk index
                         */
                        long chunkIndex = start / CHUNK_SIZE;

                        /*
                         * Offset inside chunk
                         */
                        long offsetInsideChunk = start % CHUNK_SIZE;

                        int index = (int) chunkIndex;

                        System.out.println(
                                        "Browser wants chunk : " + index);

                        /*
                         * Chunk file
                         */
                        File chunkFile = new File(
                                        videoID
                                                        + "/chunk_"
                                                        + chunkIndex
                                                        + ".mp4."
                                                        + chunkIndex);

                        /*
                         * ==========================================
                         * CHUNK NOT AVAILABLE
                         * ==========================================
                         */
                        if (!chunkFile.exists()) {

                                System.out.println(
                                                "Chunk " + index + " not available");

                                /*
                                 * Request download
                                 */

                                for (int i = 0; i < 2; i++) {

                                        DownloadVideoTask task = new DownloadVideoTask();

                                        task.setIndex(index + i);
                                        task.setVideoId(videoID);

                                        downloadVideoChunkQueue.addTask(task);
                                }

                                /*
                                 * Wait for downloader
                                 */
                                waitForChunk(
                                                chunkFile,
                                                index);
                        }

                        /*
                         * ==========================================
                         * CHUNK IS AVAILABLE
                         * ==========================================
                         */

                        System.out.println(
                                        "Chunk " + index + " available");

                        try (
                                        RandomAccessFile raf = new RandomAccessFile(
                                                        chunkFile,
                                                        "r")) {

                                /*
                                 * Move to correct position
                                 */
                                raf.seek(offsetInsideChunk);

                                /*
                                 * Remaining bytes in this chunk
                                 */
                                long remainingInChunk = CHUNK_SIZE - offsetInsideChunk;

                                /*
                                 * But last chunk may be smaller
                                 */
                                long actualChunkSize = raf.length();

                                remainingInChunk = Math.min(
                                                remainingInChunk,
                                                actualChunkSize
                                                                - offsetInsideChunk);

                                /*
                                 * Bytes to read
                                 */
                                long bytesToRead = Math.min(
                                                remainingInChunk,
                                                bytesRemaining);

                                long totalReadFromChunk = 0;

                                /*
                                 * Read chunk
                                 */
                                while (totalReadFromChunk < bytesToRead) {

                                        int currentReadSize = (int) Math.min(
                                                        buffer.length,
                                                        bytesToRead
                                                                        - totalReadFromChunk);

                                        int bytesRead = raf.read(
                                                        buffer,
                                                        0,
                                                        currentReadSize);

                                        if (bytesRead == -1) {
                                                break;
                                        }

                                        /*
                                         * Send data to browser
                                         */
                                        outputStream.write(
                                                        buffer,
                                                        0,
                                                        bytesRead);

                                        totalReadFromChunk += bytesRead;

                                        start += bytesRead;

                                        bytesRemaining -= bytesRead;
                                }
                        }

                        outputStream.flush();

                        streamingSessionQueue.setStreamSession(
                                        videoID,
                                        System.currentTimeMillis());
                }
        }

        /*
         * ==========================================
         * WAIT FOR CHUNK
         * ==========================================
         */

        private void waitForChunk(
                        File chunkFile,
                        int index) throws InterruptedException {

                long startWait = System.currentTimeMillis();

                while (!chunkFile.exists()) {

                        long elapsed = System.currentTimeMillis() - startWait;

                        /*
                         * Timeout
                         */
                        if (elapsed >= CHUNK_WAIT_TIMEOUT) {

                                System.out.println("Timeout waiting for chunk " + index);

                                throw new ChunkNotReadyException("Chunk " + index + " is still downloading");
                        }

                        Thread.sleep(CHUNK_CHECK_INTERVAL);
                }

                System.out.println(
                                "Chunk "
                                                + index
                                                + " downloaded successfully");
        }
}