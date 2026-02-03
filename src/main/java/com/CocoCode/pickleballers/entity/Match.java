package com.CocoCode.pickleballers.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player playerA;

    @ManyToOne
    private Player playerB;

    private String score; // e.g., "11-9, 7-11, 11-8"

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private String idempotencyKey; // ensures single processing

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        CONFIRMED
    }
}
