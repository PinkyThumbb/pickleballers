package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.dto.MatchEventResponseDTO;
import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.entity.MatchEvent;
import com.CocoCode.pickleballers.repository.MatchEventRepository;
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
    private final MatchEventRepository matchEventRepository;
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
        Match saved = matchRepository.save(existing);

        if (existing.getStatus() == Match.Status.CONFIRMED) {
            recordEvent(saved, MatchEvent.EventType.RESOLVED, incoming.getIdempotencyKey(), incoming.getScore());
        } else if (existing.getStatus() == Match.Status.DISPUTED) {
            recordEvent(saved, MatchEvent.EventType.DISPUTED, incoming.getIdempotencyKey(), incoming.getScore());
        }
        return saved;
    }

    Match createPending(Match match) {
        match.setStatus(Match.Status.PENDING);
        Match saved = matchRepository.save(match);
        recordEvent(saved, MatchEvent.EventType.CREATED, saved.getIdempotencyKey(), saved.getScore());
        return saved;
    }

    Optional<Match> findExistingMatch(long playerAId, long playerBId) {
        return matchRepository.findPendingOrDisputedMatchBetweenPlayers(
                List.of(Match.Status.PENDING, Match.Status.DISPUTED),
                playerAId,
                playerBId
        );
    }

    public List<MatchEventResponseDTO> getMatchHistory(Long matchId) {
        return matchEventRepository.findByMatchIdOrderByCreatedAtAsc(matchId)
                .stream()
                .map(MatchEventResponseDTO::from)
                .toList();
    }

    Optional<Match> findByIdempotencyKey(String key) {
        return matchRepository.findByIdempotencyKey(key);
    }

    public void recordEvent(Match match, MatchEvent.EventType type, String key, String score) {
        matchEventRepository.save(new MatchEvent(match, type, key, score));
    }

}
