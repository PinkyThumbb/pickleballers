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

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        CONFIRMED
    }
}
