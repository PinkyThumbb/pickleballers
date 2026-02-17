package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.dto.CreateMatchRequestDTO;
import com.CocoCode.pickleballers.dto.CreateMatchResponseDTO;
import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;

import com.CocoCode.pickleballers.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public CreateMatchResponseDTO createMatch(CreateMatchRequestDTO request, String idempotencyKey) {

        Pair normalized = normalizePlayers(
                request.getPlayerAId(),
                request.getPlayerBId()
        );

        long a = normalized.a();
        long b = normalized.b();

        Match match = new Match(
                playerRepository.getReferenceById(a),
                playerRepository.getReferenceById(b),
                normalizeScore(request.getScore()),
                Match.Status.PENDING,
                idempotencyKey,
                LocalDateTime.now()
        );

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

    public Match saveMatch(Match match) {

        if (match.getIdempotencyKey() == null || match.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("Idempotency key required");
        }

        try {
            Optional<Match> pending = matchRepository
                    .findPendingOrDisputedMatchBetweenPlayers(
                            List.of(Match.Status.PENDING, Match.Status.DISPUTED),
                            match.getPlayerA().getId(),
                            match.getPlayerB().getId()
                    );

            if (pending.isEmpty()) {
                return createPending(match);
            }

            Match existing = pending.get();

            if (isSameRequest(existing, match)) {
                return existing;
            }

            if (existing.getStatus() == Match.Status.PENDING) {
                return resolvePending(existing, match);
            }

            return existing;

        } catch (DataIntegrityViolationException e) {
            log.info("Idempotency collision for key {}", match.getIdempotencyKey());

            // If insert failed because idempotency key already exists
            return matchRepository
                    .findByIdempotencyKey(match.getIdempotencyKey())
                    .orElseThrow(() -> e);
        }
    }

    private boolean isSameRequest(Match existing, Match incoming) {
        return existing.getIdempotencyKey().equals(incoming.getIdempotencyKey());
    }

    private Match resolvePending(Match existing, Match incoming) {
        existing.resolveAgainst(incoming.getScore());
        return matchRepository.save(existing);
    }

    private Match createPending(Match match) {
        match.setStatus(Match.Status.PENDING);
        return matchRepository.save(match);
    }

    private String normalizeScore(String score) {
        if (score == null || score.isBlank()) {
            throw new IllegalArgumentException("Score required");
        }
        return score.replaceAll("\\s+", "");
    }

    private record Pair(long a, long b) {}

    private Pair normalizePlayers(long a, long b) {
        if (a == b) throw new IllegalArgumentException("Player cannot play themselves");
        return a <= b ? new Pair(a, b) : new Pair(b, a);
    }
}
