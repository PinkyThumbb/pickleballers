package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;
import com.CocoCode.pickleballers.service.MatchService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Data
@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchRepository repository;
    private final MatchService matchService;

    @GetMapping("/getMatches")
    public List<Match> all() {
        return repository.findAll();
    }

    @PostMapping("/createMatch")
    public Match submit(@RequestBody Match match) {
        // Idempotency check
        return matchService.saveMatch(match);
//        return repository.findByIdempotencyKey(match.getIdempotencyKey())
//                .orElseGet(() -> repository.save(match));
    }
}
