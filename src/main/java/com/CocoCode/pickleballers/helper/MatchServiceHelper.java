package com.CocoCode.pickleballers.helper;

import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MatchServiceHelper {

    private final MatchRepository matchRepository;

    public boolean isSameRequest(Match existing, Match incoming) {
        return existing.getIdempotencyKey().equals(incoming.getIdempotencyKey());
    }

    public Match resolvePending(Match existing, Match incoming) {
        existing.resolveAgainst(incoming.getScore());
        return matchRepository.save(existing);
    }

    public Match createPending(Match match) {
        match.setStatus(Match.Status.PENDING);
        return matchRepository.save(match);
    }

    public Optional<Match> findExistingMatch(long playerAId, long playerBId) {
        return matchRepository.findPendingOrDisputedMatchBetweenPlayers(
                List.of(Match.Status.PENDING, Match.Status.DISPUTED),
                playerAId,
                playerBId
        );
    }

    public Optional<Match> findByIdempotencyKey(String key) {
        return matchRepository.findByIdempotencyKey(key);
    }

}
