package com.CocoCode.pickleballers.validator;

import org.springframework.stereotype.Component;

@Component
public class ScoreValidator {

    public String normalizeScore(String score) {
        if (score == null || score.isBlank()) {
            throw new IllegalArgumentException("Score required");
        }

        String normalized = score.replaceAll("\\s+", "");

        String[] parts = normalized.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Score must be in format X-Y (e.g. 11-9)");
        }

        int a, b;
        try {
            a = Integer.parseInt(parts[0]);
            b = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Score values must be integers");
        }

        if (a < 0 || b < 0) {
            throw new IllegalArgumentException("Score values must be non-negative");
        }

        if (a == b) {
            throw new IllegalArgumentException("Score cannot be tied");
        }

        int winner = Math.max(a, b);
        int loser = Math.min(a, b);

        if (winner < 11) {
            throw new IllegalArgumentException("Winner must have at least 11 points");
        }

        if (winner - loser < 2) {
            throw new IllegalArgumentException("Winner must win by at least 2 points");
        }

        return normalized;
    }
}
