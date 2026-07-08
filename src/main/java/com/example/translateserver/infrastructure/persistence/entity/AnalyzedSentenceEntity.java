package com.example.translateserver.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "analyzed_sentences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzedSentenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private ChapterEntity chapter;

    @Column(name = "sentence_order", nullable = false)
    private int sentenceOrder;

    @Column(name = "vi_text", nullable = false, columnDefinition = "TEXT")
    private String viText;

    @Column(name = "en_text", nullable = false, columnDefinition = "TEXT")
    private String enText;

    @Column(length = 100)
    @Builder.Default
    private String tense = "Unknown";

    @Column(name = "words_json_meta", nullable = false, columnDefinition = "LONGTEXT")
    private String wordsJsonMeta;
}
