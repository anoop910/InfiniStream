package com.anoop.videoStream.controller;

import com.anoop.videoStream.Model.ChunkUploadTask;
import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.Model.User;
import com.anoop.videoStream.dto.CreateVideo;
import com.anoop.videoStream.memory.VideoFolderMapToChunk;
import com.anoop.videoStream.queue.VideoChunkQueue;
import com.anoop.videoStream.repository.FullVideoRepo;
import com.anoop.videoStream.repository.UserRepository;
import com.anoop.videoStream.util.FileOperation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.aspectj.lang.annotation.RequiredTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private FileOperation fileOperation;

    private VideoFolderMapToChunk videoFolderMapToChunk;
    private UserRepository userRepository;

    public UploadController(VideoChunkQueue videoChunkQueue, FullVideoRepo fullVideoRepo,
            FileOperation fileOperation, VideoFolderMapToChunk videoFolderMapToChunk, UserRepository userRepository) {
        this.videoChunkQueue = videoChunkQueue;
        this.fullVideoRepo = fullVideoRepo;
        this.fileOperation = fileOperation;
        this.videoFolderMapToChunk = videoFolderMapToChunk;
        this.userRepository = userRepository;

    }

    // Map<String, Long> map = new HashMap<>(5);

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

        Path videoFolderPath = videoFolderMapToChunk.getVideoFolderPath(videoID);

        Path saveChunk = fileOperation.saveChunk(data, videoFolderPath, chunkIndex);

        // Submit blocks if 5 buffer slots full — pure backpressure
        String taskToQueue = videoChunkQueue.setTaskToQueue(
                new ChunkUploadTask(fileName, chunkIndex, saveChunk, videoID, totalChunk));
        ;

        System.out.println(taskToQueue);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Chunk " + chunkIndex + " accepted into pipeline");
    }

    @PostMapping("/uploadvideo")
    public ResponseEntity<String> createVideoEntity(@RequestBody CreateVideo createVideo,
            Authentication authentication) throws IOException {

        FullVideo video = new FullVideo();
        video.setFileName(createVideo.getFileName());
        video.setDuration(createVideo.getVideoDuration());
        video.setHeight(createVideo.getHeight());
        video.setWidth(createVideo.getWidth());
        video.setTotalChunk(createVideo.getTotalChunk());
        video.setTotalSize(createVideo.getTotalSize());
        video.setVideoID(createVideo.getVideoID());
        String user = authentication.getName();
        Long userId = Long.parseLong(user);
        User userById = userRepository.findById(userId).get();
        video.setUser(userById);

       

     
        String uri = "./upload";
        // create folder
        Path videoFolder = fileOperation.createVideoFolder(createVideo.getVideoID(), uri);
        videoFolderMapToChunk.setVideoFolderPath(createVideo.getVideoID(), videoFolder);

        fullVideoRepo.save(video); // <-- THIS was missing

        return ResponseEntity.ok("Video registered. Start uploading chunks.");
    }

    @GetMapping("/test")
    public String testAuthencation(){
    return "App Running";
    }

}