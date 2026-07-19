package com.anoop.videoStream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetMyVideoResponse {
    private String fileName;
    private String videoId;
    private Double width;
    private Double height;
    private Double duration;
}
