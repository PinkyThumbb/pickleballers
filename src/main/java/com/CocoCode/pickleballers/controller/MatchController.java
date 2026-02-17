package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.dto.CreateMatchRequestDTO;
import com.CocoCode.pickleballers.dto.CreateMatchResponseDTO;
import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;
import com.CocoCode.pickleballers.service.MatchService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/matches")
public class MatchController {

    private final MatchRepository repository;
    private final MatchService matchService;

    @GetMapping("/getMatches")
    public List<Match> all() {
        return repository.findAll();
    }

    @PostMapping("/createMatch")
    public ResponseEntity<CreateMatchResponseDTO> createMatch(
            @Valid @RequestBody CreateMatchRequestDTO match,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        CreateMatchResponseDTO createdMatch = matchService.createMatch(match, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
    }
}
