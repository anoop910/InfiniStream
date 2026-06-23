// package com.anoop.videoStream.service;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import com.anoop.videoStream.Model.FullVideo;
// import com.anoop.videoStream.Model.VideoChunk;
// import com.anoop.videoStream.dto.CreateVideo;
// import com.anoop.videoStream.dto.TelegramResponse;
// import com.anoop.videoStream.repository.FullVideoRepo;
// import com.anoop.videoStream.repository.VideoChunkRepo;

// @Service
// public class UploadService {

//     @Autowired
//     private FullVideoRepo fullVideoRepo;

//     @Autowired
//     private VideoChunkRepo videoChunkRepo;



    
//     public void saveVideoToDB(){


//     }


// //     @Autowired
// //     private CreateVideoMataData createVideoMataData;

// //     // Thread-safe maps
// //     private final Map<String, FullVideo> mapVideoToChunk = new ConcurrentHashMap<>();
// //     private final Map<String, ConcurrentHashMap<Integer, Long>> videoChunkId = new ConcurrentHashMap<>();

// //     public void createVideo(CreateVideo video) {
// //         FullVideo fullVideo = new FullVideo();
// //         fullVideo.setDuration(video.getDuration());
// //         fullVideo.setHeight(video.getHeight());
// //         fullVideo.setWidth(video.getWidth());
// //         fullVideo.setTotalChunk(video.getTotalChunk());
// //         fullVideo.setTotalSize(video.getTotalSize());
// //         fullVideo.setVideoID(video.getVideoID());

// //         createVideoMataData.saveVideo(video.getVideoID(), video);
// //     }

// //     public void saveUploadVideo(int chunkIndex, String videoID, Long originalVideoID) {

// //         // chunk 0 always fetches from DB and caches
// //         if (chunkIndex == 0) {
// //           CreateVideo video = createVideoMataData.getVideo(videoID);

           
// //         } else {
// //             FullVideo fullVideo = mapVideoToChunk.get(videoID);

// //             if (fullVideo == null) {
// //                 // Safety: chunk 0 may not have arrived yet — fetch from DB
// //                 fullVideo = fullVideoRepo.findById(originalVideoID)
// //                         .orElseThrow(() -> new RuntimeException("Video not found: " + originalVideoID));
// //                 mapVideoToChunk.put(videoID, fullVideo);
// //             }

// //             saveChunkToDB(fullVideo, chunkIndex, videoID);
// //         }
// //     }

// //     private void saveChunkToDB(FullVideo fullVideo, int chunkIndex, String videoID) {
// //         VideoChunk videoChunk = new VideoChunk();
// //         videoChunk.setChunkIndex(chunkIndex);
// //         videoChunk.setVideoID(videoID);
// //         videoChunk.setFullVideo(fullVideo);

// //         VideoChunk saved = videoChunkRepo.save(videoChunk);

// //         // Store per chunkIndex, not just per videoID
// //         videoChunkId
// //             .computeIfAbsent(videoID, k -> new ConcurrentHashMap<>())
// //             .put(chunkIndex, saved.getId());
// //     }

// //     public void updateChunk(TelegramResponse telegramResponse, String videoID, int chunkIndex) {
// //         ConcurrentHashMap<Integer, Long> chunkMap = videoChunkId.get(videoID);

// //         if (chunkMap == null || !chunkMap.containsKey(chunkIndex)) {
// //             throw new RuntimeException("No saved chunk found for: " + videoID + " index: " + chunkIndex);
// //         }

// //         Long dbRowId = chunkMap.get(chunkIndex);

// //         VideoChunk chunk = videoChunkRepo.findById(dbRowId)
// //                 .orElseThrow(() -> new RuntimeException("Chunk row not found: " + dbRowId));

// //         chunk.setTelegramFileId(
// //             telegramResponse.getResult().getDocument().getFile_id());
// //         chunk.setTelegramUniqueId(
// //             telegramResponse.getResult().getDocument().getFile_unique_id());

// //         videoChunkRepo.save(chunk);
// //     }

// //     public void deleteMapVideoToChunk(String videoID) {
// //         mapVideoToChunk.remove(videoID);
// //         videoChunkId.remove(videoID);
// //     }
//  }