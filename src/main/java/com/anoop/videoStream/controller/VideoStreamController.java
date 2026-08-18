package com.anoop.videoStream.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.anoop.videoStream.exception.ChunkNotReadyException;
import com.anoop.videoStream.memory.StreamSession;
import com.anoop.videoStream.memory.StreamSessionManager;
import com.anoop.videoStream.stream.streamService.VirtualVideoStreamService;

@RestController
@CrossOrigin(origins= "*")
public class VideoStreamController {

    private final VirtualVideoStreamService streamService;

    private final StreamSessionManager sessionManager;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1 MB

    public VideoStreamController(
            VirtualVideoStreamService streamService,
            StreamSessionManager sessionManager) {

        this.streamService = streamService;
        this.sessionManager = sessionManager;
    }

    @GetMapping("/video/{videoID}")
    public ResponseEntity<StreamingResponseBody> streamVideo(
            @PathVariable String videoID,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        System.out.println(
                "Request range : " + rangeHeader);

        StreamSession session = sessionManager.getOrCreateStreamSession(videoID);

        try {

            long fileSize = session.getTotalSize();

            long start = 0;

            long end = fileSize - 1;

            /*
             * =====================================
             * RANGE HEADER
             * =====================================
             */

            if (rangeHeader != null
                    && rangeHeader.startsWith("bytes=")) {

                String[] ranges = rangeHeader
                        .replace("bytes=", "")
                        .split("-");

                start = Long.parseLong(ranges[0]);

                System.out.println(
                        "ranges : " + start);

                /*
                 * Browser provided END
                 */
                if (ranges.length > 1
                        && !ranges[1].isEmpty()) {

                    end = Long.parseLong(
                            ranges[1]);

                    System.out.println(
                            "second condition : "
                                    + end);

                } else {

                    /*
                     * Send maximum 1 MB
                     */
                    end = Math.min(
                            start + CHUNK_SIZE - 1,
                            fileSize - 1);

                    System.out.println(
                            "else condition : "
                                    + end);
                }
            }

            /*
             * =====================================
             * VALIDATE RANGE
             * =====================================
             */

            if (start < 0
                    || start >= fileSize
                    || end < start) {

                return ResponseEntity
                        .status(
                                HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .build();
            }

            /*
             * Don't allow end beyond file
             */
            end = Math.min(
                    end,
                    fileSize - 1);

            long contentLength = end - start + 1;

            System.out.println(
                    "final start : " + start);

            System.out.println(
                    "final end : " + end);

            System.out.println(
                    "content length : "
                            + contentLength);

            /*
             * =====================================
             * CREATE STREAM
             * =====================================
             */

            long finalStart = start;

            long finalEnd = end;

            StreamingResponseBody responseBody = outputStream -> {

                try {

                    streamService.streamVideo(
                            videoID,
                            finalStart,
                            finalEnd,
                            outputStream);

                } catch (Exception e) {

                    /*
                     * IMPORTANT:
                     *
                     * At this point HTTP headers
                     * are already sent.
                     *
                     * We cannot change 206 to 503.
                     */

                    System.err.println(
                            "Streaming error: "
                                    + e.getMessage());

                    e.printStackTrace();
                }
            };

            /*
             * =====================================
             * HTTP HEADERS
             * =====================================
             */

            HttpHeaders headers = new HttpHeaders();

            headers.add(
                    "Accept-Ranges",
                    "bytes");

            headers.add(
                    "Content-Range",
                    "bytes "
                            + start
                            + "-"
                            + end
                            + "/"
                            + fileSize);

            headers.setContentLength(
                    contentLength);

            return ResponseEntity
                    .status(
                            HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(
                            MediaType.parseMediaType(
                                    "video/mp4"))
                    .body(responseBody);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // @GetMapping("/stream/{videoId}")
    // public void streamVideo(
    // @PathVariable String videoId) throws Exception {

    // downloadVideoChunk.streamVideo(videoId);
    // }
}