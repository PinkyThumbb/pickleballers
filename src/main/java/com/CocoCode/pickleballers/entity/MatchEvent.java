package com.CocoCode.pickleballers.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "match_event")
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(name = "triggered_by_key")
    private String triggeredByKey;

    private String score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum EventType {
        CREATED,
        RESOLVED,
        DISPUTED,
        IDEMPOTENT_DUPLICATE
    }

    public MatchEvent(Match match, EventType eventType, String triggeredByKey, String score) {
        this.match = match;
        this.eventType = eventType;
        this.triggeredByKey = triggeredByKey;
        this.score = score;
    }
}
