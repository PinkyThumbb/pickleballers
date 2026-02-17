package com.CocoCode.pickleballers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchRequestDTO {

    @NotNull
    private Long playerAId;

    @NotNull
    private Long playerBId;

    @NotBlank
    private String score;
}
