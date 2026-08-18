package com.anoop.videoStream.telegramService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.Model.VideoChunk;
import com.anoop.videoStream.memory.UploadSession;
import com.anoop.videoStream.memory.VideoFolderMapToChunk;
import com.anoop.videoStream.repository.FullVideoRepo;
import com.anoop.videoStream.repository.VideoChunkRepo;


@Service

public class DatabaseFlushService {

        private VideoChunkRepo videoChunkRepo;
        private FullVideoRepo fullVideoRepo;
        private VideoFolderMapToChunk videoFolderMapToChunk;

        

        public DatabaseFlushService(VideoChunkRepo videoChunkRepo, 
                VideoFolderMapToChunk videoFolderMapToChunk, FullVideoRepo fullVideoRepo) {
                this.videoChunkRepo = videoChunkRepo;
                this.videoFolderMapToChunk = videoFolderMapToChunk;
                this.fullVideoRepo = fullVideoRepo;
        }



        public void flushToDatabase(UploadSession session) {
                List<VideoChunk> chunks = new ArrayList<>(session.getChunks().values());
              
                FullVideo byVideoID = fullVideoRepo.findByVideoID(session.getVideoId()).get();


                chunks.sort(Comparator.comparingInt(VideoChunk::getChunkIndex));
                chunks.forEach(System.out::print);
                System.out.println("SAVING " + chunks.size() + " CHUNKS TO DB");

                byVideoID.setUploadedChunks((int)session.getUploadedChunks().get());
                byVideoID.setCompletedAt(LocalDateTime.now());
                fullVideoRepo.save(byVideoID);
               

                videoChunkRepo.saveAll(chunks);

               

                // save FullVideo

                // save VideoChunk list
        }
}