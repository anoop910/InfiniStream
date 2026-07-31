package com.anoop.videoStream.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class FileOperation {

    public Path createVideoFolder(String videoId, String uri) throws IOException  {

        Path folder = Paths.get(uri,videoId);
        
        Files.createDirectories(folder);

        return folder;
    }

    public Path saveChunk(byte[] data, Path folder, int chunkIndex) throws IOException {

        Path chunkFile = folder.resolve("chunk_" + chunkIndex + ".mp4" + "." + chunkIndex);

        return Files.write( chunkFile,data);

    }

    public void deleteDirectory(Path directory) throws IOException {

    try (Stream<Path> paths = Files.walk(directory)) {
        System.out.println("directory deleted");

        List<Path> pathsToDelete = paths
                .sorted(Comparator.reverseOrder())
                .toList();

        for (Path path : pathsToDelete) {
            Files.deleteIfExists(path);
        }

        
    }
}
}
