package com.anoop.videoStream.stream.streamService;

import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.DownloadVideoTask;
import com.anoop.videoStream.queue.DownloadVideoChunkQueue;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.OutputStream;

@Service
public class VirtualVideoStreamService {

        // SAME chunk size used during upload
        private static final long CHUNK_SIZE = 1024 * 1024; // 1 MB

        private static final int BUFFER_SIZE = 64 * 1024; // 64 KB

        private DownloadVideoChunk downloadVideoChunk;

        private DownloadVideoChunkQueue downloadVideoChunkQueue;

        public VirtualVideoStreamService(DownloadVideoChunk downloadVideoChunk,
                        DownloadVideoChunkQueue downloadVideoChunkQueue) {
                this.downloadVideoChunk = downloadVideoChunk;
                this.downloadVideoChunkQueue = downloadVideoChunkQueue;
        }

        public void streamVideo(String videoID, long start, long end, OutputStream outputStream) throws Exception {
                System.out.println("file name : " + videoID);
                System.out.println("start : " + start);
                System.out.println("end :" + end);
                long bytesRemaining = end - start + 1;
                System.out.println("Remaining bytes :" + bytesRemaining);

                byte[] buffer = new byte[BUFFER_SIZE];

                while (bytesRemaining > 0) {

                        /*
                         * Find chunk index
                         */
                        long chunkIndex = start / CHUNK_SIZE;

                        /*
                         * Offset INSIDE chunk
                         */
                        long offsetInsideChunk = start % CHUNK_SIZE;

                        /*
                         * Chunk file
                         */

                        System.out.println("Browser wants chunk : " + chunkIndex);

                        int index = (int) chunkIndex;

                        File chunkFile = new File(videoID + "/" + "chunk_" + chunkIndex + ".mp4" + "." + chunkIndex);

                        if (!chunkFile.exists()) {

                                for (int i = 0; i < 2; i++) {
                                        DownloadVideoTask task = new DownloadVideoTask();
                                        task.setIndex(index + i);
                                        task.setVideoId(videoID);

                                        downloadVideoChunkQueue.setTaskToQueue(task);

                                }

                                long startWait = System.currentTimeMillis();

                                while (!chunkFile.exists()) {

                                        if (System.currentTimeMillis() - startWait > 30000) {
                                                throw new RuntimeException(
                                                                "Timeout waiting for chunk " + index);
                                        }

                                        Thread.sleep(100);
                                }
                        }

                        /*
                         * Open chunk
                         */

                        if (!chunkFile.exists()) {

                        }
                        RandomAccessFile raf = new RandomAccessFile(chunkFile, "r");

                        /*
                         * Jump to exact position
                         */
                        raf.seek(offsetInsideChunk);

                        /*
                         * Remaining bytes in current chunk
                         */
                        long remainingInChunk = CHUNK_SIZE - offsetInsideChunk;

                        /*
                         * How many bytes should read NOW
                         */
                        long bytesToRead = Math.min(remainingInChunk, bytesRemaining);

                        long totalReadFromChunk = 0;

                        while (totalReadFromChunk < bytesToRead) {

                                int currentReadSize = (int) Math.min(buffer.length, bytesToRead - totalReadFromChunk);

                                int bytesRead = raf.read(buffer, 0, currentReadSize);

                                if (bytesRead == -1) {
                                        break;
                                }

                                /*
                                 * Send directly to browser
                                 */
                                outputStream.write(buffer, 0, bytesRead);

                                totalReadFromChunk += bytesRead;

                                start += bytesRead;

                                bytesRemaining -= bytesRead;
                        }
                        outputStream.flush();

                        raf.close();
                }
        }

}