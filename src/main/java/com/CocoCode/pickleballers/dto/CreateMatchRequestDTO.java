package com.CocoCode.pickleballers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchRequestDTO {
    private Long playerAId;
    private Long playerBId;
    private String score;
}
