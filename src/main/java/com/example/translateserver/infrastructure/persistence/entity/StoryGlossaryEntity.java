package com.example.translateserver.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "story_glossaries",
    uniqueConstraints = @UniqueConstraint(name = "unique_story_term", columnNames = {"story_id", "vi_term"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryGlossaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private StoryEntity story;

    @Column(name = "vi_term", nullable = false, length = 255)
    private String viTerm;

    @Column(name = "en_term", nullable = false, length = 255)
    private String enTerm;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_manual_addition")
    @Builder.Default
    private boolean isManualAddition = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
