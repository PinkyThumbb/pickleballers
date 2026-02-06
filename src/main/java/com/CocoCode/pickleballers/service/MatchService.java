package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    @Transactional
    public Match saveMatch(Match match) {
        // Check for an existing match between these two players that is PENDING
        Optional<Match> existing = matchRepository
                .findPendingMatchBetweenPlayers(Match.Status.PENDING, match.getPlayerA().getId(), match.getPlayerB().getId());

        // If found, return the existing match (idempotency)
        // Otherwise, save the new match
        if (existing.isEmpty()) {
            String key = Optional.ofNullable(match.getIdempotencyKey())
                    .orElse(UUID.randomUUID().toString());
            match.setIdempotencyKey(key);
            matchRepository.save(match);
        }
        else {
            existing.get().setStatus(Match.Status.CONFIRMED);
            matchRepository.save(existing.get());
        }
        return match;
    }
}
