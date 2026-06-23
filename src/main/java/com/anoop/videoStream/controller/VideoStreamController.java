package com.anoop.videoStream.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.anoop.videoStream.memory.StreamSession;
import com.anoop.videoStream.memory.StreamSessionManager;
import com.anoop.videoStream.stream.streamService.DownloadVideoChunk;
import com.anoop.videoStream.stream.streamService.VirtualVideoStreamService;


@RestController
public class VideoStreamController {

    @Autowired
    private VirtualVideoStreamService streamService;

    @Autowired
    private DownloadVideoChunk downloadVideoChunk;

    @Autowired
    private StreamSessionManager sessionManager;

    // ORIGINAL FILE SIZE
    // Store this in DB in production
    private static long FILE_SIZE = 500000000L;

    @GetMapping("/video/{videoID}")
    public ResponseEntity<StreamingResponseBody> streamVideo(@PathVariable String videoID,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        System.out.println("Request range :" + rangeHeader);

       StreamSession orCreateStreamSession = sessionManager.getOrCreateStreamSession(videoID);
    

        try {

            FILE_SIZE = orCreateStreamSession.getTotalSize();

            long start = 0;

            long end = FILE_SIZE - 1;

            /*
             * Parse Range Header
             */
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {

                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                System.out.println("ranges : " + ranges[0]);
                start = Long.parseLong(ranges[0]);

                /*
                 * Optional browser end range
                 */
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                    System.out.println("second condition" + end);
                } else {

                    /*
                     * Default chunk response size
                     */
                    end = Math.min(start + (1024 * 1024) - 1, FILE_SIZE - 1);
                    System.out.println("else condition" + end);
                }
            }

            long contentLength = end - start + 1;

            long finalStart = start;
            System.out.println("final strat " + finalStart);

            long finalEnd = end;
            System.out.println("final end  " + finalEnd);

            StreamingResponseBody responseBody = outputStream -> {

                try {
                    streamService.streamVideo(videoID, finalStart, finalEnd, outputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            HttpHeaders headers = new HttpHeaders();

            headers.add("Accept-Ranges", "bytes");

            headers.add("Content-Range", "bytes " + start + "-" + end + "/" + FILE_SIZE);

            headers.setContentLength(contentLength);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers)
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .body(responseBody);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @GetMapping("/stream/{videoId}")
    public void streamVideo(
            @PathVariable String videoId) throws Exception {

        downloadVideoChunk.streamVideo(videoId);
    }
}