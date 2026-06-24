package com.anoop.videoStream.controller;

import com.anoop.videoStream.Model.ChunkUploadTask;
import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.queue.VideoChunkQueue;
import com.anoop.videoStream.repository.FullVideoRepo;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@CrossOrigin(origins = "*")
public class UploadController {

    // @Autowired
    // private TelegramUploadService telegramUploadService;

    
    private VideoChunkQueue videoChunkQueue;
    // @Autowired
    // private UploadService uploadService;


   
    private FullVideoRepo fullVideoRepo;




 

    public UploadController(VideoChunkQueue videoChunkQueue, FullVideoRepo fullVideoRepo) {
        this.videoChunkQueue = videoChunkQueue;
        this.fullVideoRepo = fullVideoRepo;
       
    }

    //Map<String, Long> map = new HashMap<>(5);

    /**
     * Browser sends one chunk at a time.
     * This call BLOCKS if the pipeline buffer is full (backpressure).
     * Browser naturally slows down — no RAM overflow possible.
     */
    // @PostMapping("/uploadfile")
    // public ResponseEntity<String> uploadChunk(
    // @RequestParam("chunk") MultipartFile chunk,
    // @RequestParam("chunkIndex") int chunkIndex,
    // @RequestParam("fileName") String fileName) throws Exception {

    // // Read bytes into memory — no disk write at all
    // byte[] data = chunk.getBytes();

    // // Submit blocks if 5 buffer slots full — pure backpressure
    // telegramUploadService.submit(
    // new ChunkUploadTask(fileName, chunkIndex, data));

    // return ResponseEntity
    // .status(HttpStatus.ACCEPTED)
    // .body("Chunk " + chunkIndex + " accepted into pipeline");
    // }

    /**
     * Health check — see live pipeline state
     * GET /upload/health
     */
    // @GetMapping("/upload/health")
    // public ResponseEntity<Map<String, Object>> health() {
    // return ResponseEntity.ok(telegramUploadService.getHealth());
    // }

    @PostMapping("/uploadtrail")
    public ResponseEntity<String> uploadChunkTrail(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("fileName") String fileName,
            @RequestParam("videoID") String videoID,
            @RequestParam("lastChunk") Boolean lastChunk,
            @RequestParam("totalChunk") int totalChunk) throws Exception {

        // Read bytes into memory — no disk write at all
        byte[] data = chunk.getBytes();

        // Submit blocks if 5 buffer slots full — pure backpressure
        String taskToQueue = videoChunkQueue.setTaskToQueue(
                new ChunkUploadTask(fileName, chunkIndex, data, videoID, totalChunk));
        ;

        System.out.println(taskToQueue);

       

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Chunk " + chunkIndex + " accepted into pipeline");
    }

    @PostMapping("/uploadvideo")
    public ResponseEntity<String> createVideoEntity(
            @RequestParam("fileName") String fileName,
            @RequestParam("videoDuration") Double videoDuration,
            @RequestParam("videoID") String videoID,
            @RequestParam("height") Double height,
            @RequestParam("width") Double width,
            @RequestParam("totalChunk") int totalChunk,
            @RequestParam("totalSize") Long totalSize) {

        FullVideo video = new FullVideo();
        video.setFileName(fileName);
        video.setDuration(videoDuration);
        video.setHeight(height);
        video.setWidth(width);
        video.setTotalChunk(totalChunk);
        video.setTotalSize(totalSize);
        video.setVideoID(videoID);
        

        fullVideoRepo.save(video); // <-- THIS was missing

        return ResponseEntity.ok("Video registered. Start uploading chunks.");
    }
}