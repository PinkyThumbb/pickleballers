package com.CocoCode.pickleballers.service;

import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.repository.MatchRepository;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@AllArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

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

            if (pending.isPresent() && !pending.get().getIdempotencyKey().equals(match.getIdempotencyKey())) {

                Match existing = pending.get();
                existing.setStatus(Match.Status.CONFIRMED);
                return matchRepository.save(existing);
            }
            else if (pending.isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key already exists for pending match"
                );
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
}
