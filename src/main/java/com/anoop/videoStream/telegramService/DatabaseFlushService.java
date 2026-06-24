package com.anoop.videoStream.telegramService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.VideoChunk;
import com.anoop.videoStream.memory.UploadSession;
import com.anoop.videoStream.repository.VideoChunkRepo;


@Service

public class DatabaseFlushService {

        private VideoChunkRepo videoChunkRepo;

        

        public DatabaseFlushService(VideoChunkRepo videoChunkRepo) {
                this.videoChunkRepo = videoChunkRepo;
        }



        public void flushToDatabase(UploadSession session) {
                List<VideoChunk> chunks = new ArrayList<>(session.getChunks().values());

                chunks.sort(Comparator.comparingInt(VideoChunk::getChunkIndex));
                chunks.forEach(System.out::print);
                System.out.println("SAVING " + chunks.size() + " CHUNKS TO DB");

                videoChunkRepo.saveAll(chunks);

                // save FullVideo

                // save VideoChunk list
        }
}