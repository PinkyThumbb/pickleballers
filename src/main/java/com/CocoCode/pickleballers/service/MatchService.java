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

                if (!existing.getIdempotencyKey().equals(match.getIdempotencyKey())
                        && existing.getScore().equalsIgnoreCase(match.getScore())) { //todo - make better score comparison and add dispute resolution process
                    existing.setStatus(Match.Status.CONFIRMED);
                    return matchRepository.save(existing);
                }
                else {
                    return existing;
                }
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

        Match match = new Match(
                playerRepository.getReferenceById(request.getPlayerAId()),
                playerRepository.getReferenceById(request.getPlayerBId()),
                request.getScore(),
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

}
