package com.anoop.videoStream.stream.streamService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anoop.videoStream.Model.FullVideo;
import com.anoop.videoStream.Model.VideoChunk;
import com.anoop.videoStream.config.TelegramWebClientConfig;
import com.anoop.videoStream.memory.StreamSession;
import com.anoop.videoStream.memory.StreamSessionManager;
import com.anoop.videoStream.repository.FullVideoRepo;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class DownloadVideoChunk {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private TelegramWebClientConfig telegramWebClientConfig;

    @Autowired
    private StreamSessionManager streamSessionManager;

  
    @Autowired
    private FullVideoRepo fullVideoRepo;

    Map<String, List<VideoChunk>> video = new HashMap<>();

    // Step A — get file_path from Telegram
    private String getFilePath(String fileId) {
        String response = telegramWebClientConfig.getWebClient()
                .get()
                .uri("/bot" + botToken + "/getFile?file_id=" + fileId)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // parse file_path from response
        // {"ok":true,"result":{"file_path":"documents/file_xyz"}}
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        return root.path("result").path("file_path").asString();
    }

    // Step B — download actual bytes
    private byte[] downloadChunk(String filePath) {
        return telegramWebClientConfig.getWebClient()
                .get()
                .uri("/file/bot" + botToken + "/" + filePath)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    // Step C — stream full video to response
    public void streamVideo(String videoId) throws Exception {

        Optional<FullVideo> byVideoID = fullVideoRepo.findByVideoID(videoId);
        List<VideoChunk> chunks = byVideoID.get().getVideoChunks();

        video.put(videoId, chunks);

        // if (chunks.isEmpty()) {
        //     throw new RuntimeException("No chunks found for video: " + videoId);
        // }

        // Path folder = createVideoFolder(videoId);

        // for (VideoChunk chunk : chunks) {
        //     System.out.println("Downloading chunk " + chunk.getChunkIndex()
        //             + " file_id: " + chunk.getTelegramFileId());

        //     String filePath = getFilePath(chunk.getTelegramFileId());
        //     byte[] data = downloadChunk(filePath);
        //     saveChunk(data, folder, chunk.getChunkIndex());

        // }
    }

    public void downloadChunkByIndex(String videoID, int index) {
        System.out.println("call revice");
       StreamSession session = streamSessionManager.getSession(videoID);
       List<VideoChunk> videoChunks = session.getVideoChunks();
        String telegramFileId = videoChunks.get(index).getTelegramFileId();
        System.out.println("Downloading chunk " + index+ " file_id: " + telegramFileId);
        String filePath = getFilePath(telegramFileId);
        byte[] data = downloadChunk(filePath);
        try {
            Path videoFolder = createVideoFolder(videoID);
            saveChunk(data, videoFolder, index);
        } catch (IOException e) {
         
            e.printStackTrace();
        }
    }

    private Path createVideoFolder(
            String videoId)
            throws IOException {

        Path folder = Paths.get(
                "D:\\videoStream",
                videoId);

        Files.createDirectories(folder);

        return folder;
    }

    private void saveChunk(
            byte[] data,
            Path folder,
            int chunkIndex)
            throws IOException {

        Path chunkFile = folder.resolve(
                "chunk_"
                        + chunkIndex
                        + ".mp4" + "." + chunkIndex);

        Files.write(
                chunkFile,
                data);

    }

}
