package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.dto.CreateMatchRequestDTO;
import com.CocoCode.pickleballers.dto.CreateMatchResponseDTO;
import com.CocoCode.pickleballers.dto.MatchEventResponseDTO;
import com.CocoCode.pickleballers.entity.Match;

import com.CocoCode.pickleballers.entity.MatchEvent;
import com.CocoCode.pickleballers.helper.PlayerServiceHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MatchService {

    private final MatchServiceHelper matchServiceHelper;
    private final PlayerServiceHelper playerServiceHelper;

    @Transactional
    public CreateMatchResponseDTO createMatch(CreateMatchRequestDTO request, String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key required");
        }

        Pair normalized = normalizePlayers(
                request.getPlayerAId(),
                request.getPlayerBId()
        );

        playerServiceHelper.validatePlayersExist(normalized.a(), normalized.b());

        Match match = matchServiceHelper.buildMatch(normalized.a(), normalized.b(), request.getScore(), idempotencyKey);

        Match saved = saveMatch(match);

        return new CreateMatchResponseDTO(
                saved.getId(),
                saved.getPlayerA().getId(),
                saved.getPlayerB().getId(),
                saved.getScore(),
                saved.getStatus(),
                saved.getIdempotencyKey(),
                saved.getCreatedAt()
        );
    }

    private Match saveMatch(Match match) {

        try {
            Optional<Match> pending = matchServiceHelper.findExistingMatch(match.getPlayerA().getId(), match.getPlayerB().getId());

            if (pending.isEmpty()) {
                return matchServiceHelper.createPending(match);
            }

            Match existing = pending.get();

            if (existing.getIdempotencyKey().equals(match.getIdempotencyKey())) {
                matchServiceHelper.recordEvent(existing, MatchEvent.EventType.IDEMPOTENT_DUPLICATE, match.getIdempotencyKey(), match.getScore());
                return existing;
            }

            if (existing.getStatus() == Match.Status.PENDING) {
                return matchServiceHelper.resolvePending(existing, match);
            }

            log.warn("Match {} is DISPUTED — returning existing without resolving", existing.getId()); //NEED TO RESOLVE DISPUTED MATCHES IN FUTURE ITERATION
            matchServiceHelper.recordEvent(existing, MatchEvent.EventType.DISPUTED, match.getIdempotencyKey(), match.getScore());
            return existing;

        } catch (DataIntegrityViolationException e) {
            log.info("Idempotency collision for key {}", match.getIdempotencyKey());

            // If insert failed because idempotency key already exists
            return matchServiceHelper
                    .findByIdempotencyKey(match.getIdempotencyKey())
                    .orElseThrow(() -> e);
        }
    }

    public List<MatchEventResponseDTO> getMatchHistory(Long matchId) {
        return matchServiceHelper.getMatchHistory(matchId);
    }

    private record Pair(long a, long b) {}

    private Pair normalizePlayers(long a, long b) {
        if (a == b) throw new IllegalArgumentException("Player cannot play themselves");
        return a <= b ? new Pair(a, b) : new Pair(b, a);
    }
}
