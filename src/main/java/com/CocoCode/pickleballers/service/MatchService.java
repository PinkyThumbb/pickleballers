package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.dto.CreateMatchRequestDTO;
import com.CocoCode.pickleballers.dto.CreateMatchResponseDTO;
import com.CocoCode.pickleballers.entity.Match;

import com.CocoCode.pickleballers.helper.MatchServiceHelper;
import com.CocoCode.pickleballers.helper.PlayerServiceHelper;
import com.CocoCode.pickleballers.repository.PlayerRepository;
import com.CocoCode.pickleballers.validator.ScoreValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MatchService {

    private final PlayerRepository playerRepository;
    private final MatchServiceHelper matchServiceHelper;
    private final PlayerServiceHelper playerServiceHelper;
    private final ScoreValidator scoreValidator;

    @Transactional
    public CreateMatchResponseDTO createMatch(CreateMatchRequestDTO request, String idempotencyKey) {

        Pair normalized = normalizePlayers(
                request.getPlayerAId(),
                request.getPlayerBId()
        );

        playerServiceHelper.validatePlayersExist(normalized.a(), normalized.b());

        Match match = buildMatch(normalized.a(), normalized.b(), request.getScore(), idempotencyKey);

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

    private Match buildMatch(long a, long b, String score, String idempotencyKey) {
        return new Match(
                playerRepository.getReferenceById(a),
                playerRepository.getReferenceById(b),
                scoreValidator.normalizeScore(score),
                Match.Status.PENDING,
                idempotencyKey,
                LocalDateTime.now()
        );
    }

    private Match saveMatch(Match match) {

        if (match.getIdempotencyKey() == null || match.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("Idempotency key required");
        }

        try {
            Optional<Match> pending = matchServiceHelper.findExistingMatch(match.getPlayerA().getId(), match.getPlayerB().getId());

            if (pending.isEmpty()) {
                return matchServiceHelper.createPending(match);
            }

            Match existing = pending.get();

            if (matchServiceHelper.isSameRequest(existing, match)) {
                return existing;
            }

            if (existing.getStatus() == Match.Status.PENDING) {
                return matchServiceHelper.resolvePending(existing, match);
            }

            return existing;

        } catch (DataIntegrityViolationException e) {
            log.info("Idempotency collision for key {}", match.getIdempotencyKey());

            // If insert failed because idempotency key already exists
            return matchServiceHelper
                    .findByIdempotencyKey(match.getIdempotencyKey())
                    .orElseThrow(() -> e);
        }
    }

    private record Pair(long a, long b) {}

    private Pair normalizePlayers(long a, long b) {
        if (a == b) throw new IllegalArgumentException("Player cannot play themselves");
        return a <= b ? new Pair(a, b) : new Pair(b, a);
    }
}
