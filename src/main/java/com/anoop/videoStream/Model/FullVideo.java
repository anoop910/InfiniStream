package com.anoop.videoStream.Model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                                "videoID",
                                "videoChunks"
                })
})
public class FullVideo {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String fileName;
        private Double width;
        private Double height;
        private Double duration;
        private int totalChunk;
        private String videoID;
        private int uploadedChunks;

        private long totalSize;

        // @Enumerated(EnumType.STRING)
        // private UploadStatus status;

        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime completedAt;
        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;
        @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "fullVideo")
        @JsonManagedReference
        private List<VideoChunk> videoChunks;

}
