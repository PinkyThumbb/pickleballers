package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Data
@Service
public class MatchService {

    private MatchRepository matchRepository;

    @Transactional
    public Match saveMatch(Match match) {
        // Check for an existing match between these two players that is PENDING
        Optional<Match> existing = matchRepository
                .findPendingMatchBetweenPlayers(Match.Status.PENDING, match.getPlayerA().getId(), match.getPlayerB().getId());

        // If found, return the existing match (idempotency)
        // Otherwise, save the new match
        if (existing.isEmpty()) {
            matchRepository.save(match);
        }
        else {
            existing.get().setStatus(Match.Status.CONFIRMED);
            matchRepository.save(existing.get());
        }
        return match;
    }
}
