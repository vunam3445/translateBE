package com.example.translateserver.infrastructure.persistence.entity;

import com.example.translateserver.domain.model.ChapterStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "chapters",
    uniqueConstraints = @UniqueConstraint(name = "unique_story_chapter", columnNames = {"story_id", "chapter_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private StoryEntity story;

    @Column(name = "chapter_number", nullable = false)
    private int chapterNumber;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING'")
    @Builder.Default
    private ChapterStatus status = ChapterStatus.PENDING;

    @Lob
    @Column(name = "raw_content", columnDefinition = "LONGTEXT")
    private String rawContent;

    @Lob
    @Column(name = "translated_content", columnDefinition = "LONGTEXT")
    private String translatedContent;

    @Column(name = "translation_progress", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private int translationProgress = 0;

    @CreationTimestamp

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnalyzedSentenceEntity> analyzedSentences = new ArrayList<>();

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AudioMarkerEntity> audioMarkers = new ArrayList<>();
}
