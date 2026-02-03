package com.CocoCode.pickleballers.repository;

import com.CocoCode.pickleballers.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByIdempotencyKey(String key);
}
