package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;
import com.CocoCode.pickleballers.service.MatchService;
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
    public ResponseEntity<Match> createMatch(
            @RequestBody Match match,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            match.setIdempotencyKey(idempotencyKey);
        }
        Match saved = matchService.saveMatch(match);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
