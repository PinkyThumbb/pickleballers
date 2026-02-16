package com.CocoCode.pickleballers.dto;

import com.CocoCode.pickleballers.entity.Match;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchResponseDTO {

    private Long id;
    private Long playerAId;
    private Long playerBId;
    private String score;
    private Match.Status status;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}
