package com.CocoCode.pickleballers.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_match_idempotency", columnNames = "idempotency_key")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player playerA;

    @ManyToOne
    private Player playerB;

    private String score;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Setter(AccessLevel.NONE)
    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    private LocalDateTime disputedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        CONFIRMED,
        DISPUTED
    }

    public Match(Player playerA,
                 Player playerB,
                 String score,
                 Status status,
                 String idempotencyKey,
                 LocalDateTime createdAt) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.score = score;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public void resolveAgainst(String incomingScore) {
        if (this.score.equals(incomingScore)) {
            this.status = Status.CONFIRMED;
        } else {
            this.status = Status.DISPUTED;
            this.disputedAt = LocalDateTime.now();
        }
    }

}
