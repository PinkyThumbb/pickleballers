package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;
import com.CocoCode.pickleballers.repository.PlayerRepository;
import com.CocoCode.pickleballers.validator.ScoreValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
class MatchServiceHelper {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final ScoreValidator scoreValidator;

    Match buildMatch(long a, long b, String score, String idempotencyKey) {
        return new Match(
                playerRepository.getReferenceById(a),
                playerRepository.getReferenceById(b),
                scoreValidator.normalizeScore(score),
                Match.Status.PENDING,
                idempotencyKey,
                LocalDateTime.now()
        );
    }

    Match resolvePending(Match existing, Match incoming) {
        existing.resolveAgainst(incoming.getScore());
        return matchRepository.save(existing);
    }

    Match createPending(Match match) {
        match.setStatus(Match.Status.PENDING);
        return matchRepository.save(match);
    }

    Optional<Match> findExistingMatch(long playerAId, long playerBId) {
        return matchRepository.findPendingOrDisputedMatchBetweenPlayers(
                List.of(Match.Status.PENDING, Match.Status.DISPUTED),
                playerAId,
                playerBId
        );
    }

    Optional<Match> findByIdempotencyKey(String key) {
        return matchRepository.findByIdempotencyKey(key);
    }

}
