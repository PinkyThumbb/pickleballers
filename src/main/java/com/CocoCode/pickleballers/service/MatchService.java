package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.dto.CreateMatchRequestDTO;
import com.CocoCode.pickleballers.dto.CreateMatchResponseDTO;
import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;

import com.CocoCode.pickleballers.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public Match saveMatch(Match match) {

        if (match.getIdempotencyKey() == null) {
            throw new IllegalArgumentException("Idempotency key required");
        }

        try {
            Optional<Match> pending = matchRepository
                    .findPendingMatchBetweenPlayers(
                            Match.Status.PENDING,
                            match.getPlayerA().getId(),
                            match.getPlayerB().getId()
                    );

            if (pending.isPresent()) {
                Match existing = pending.get();

                if (!existing.getIdempotencyKey().equals(match.getIdempotencyKey())) {

                    if (scoresMatch(existing, match)) {
                        existing.setStatus(Match.Status.CONFIRMED);
                    } else {
                        existing.setStatus(Match.Status.DISPUTED);
                        existing.setDisputedAt(LocalDateTime.now());
                    }

                    return matchRepository.save(existing);
                }

                return existing;

            }

            match.setStatus(Match.Status.PENDING);
            return matchRepository.save(match);

        } catch (DataIntegrityViolationException e) {

            // If insert failed because idempotency key already exists
            return matchRepository
                    .findByIdempotencyKey(match.getIdempotencyKey())
                    .orElseThrow(() -> e);
        }
    }

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

    private boolean scoresMatch(Match existing, Match incoming) {
        return normalizeScore(existing.getScore())
                .equals(normalizeScore(incoming.getScore()));
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
