package com.anoop.videoStream.Model;

public class ChunkUploadTask {

    private final String fileName;
    private final int    chunkIndex;
    private final byte[] data;
    private int          retryCount = 0; // needed for priority queue ordering
    private String videoID;
    private int totalChunk;

    public ChunkUploadTask(String fileName, int chunkIndex, byte[] data, String videoID, int totalChunk) {
        this.fileName   = fileName;
        this.chunkIndex = chunkIndex;
        this.data       = data;
        this.videoID  = videoID;
        this.totalChunk = totalChunk;
    }

    public String getFileName()  { return fileName;   }
    public int    getChunkIndex(){ return chunkIndex;  }
    public byte[] getData()      { return data;        }
    public int    getRetryCount(){ return retryCount;  }
    

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }
    

    public int getTotalChunk() {
        return totalChunk;
    }

    public void setTotalChunk(int totalChunk) {
        this.totalChunk = totalChunk;
    }

    public void incrementRetry() { retryCount++;       }
}