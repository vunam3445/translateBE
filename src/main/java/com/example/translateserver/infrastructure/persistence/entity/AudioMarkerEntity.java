package com.example.translateserver.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audio_markers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioMarkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private ChapterEntity chapter;

    @Column(name = "sentence_order", nullable = false)
    private int sentenceOrder;

    @Column(name = "start_time_ms", nullable = false)
    private int startTimeMs;

    @Column(name = "end_time_ms", nullable = false)
    private int endTimeMs;
}
