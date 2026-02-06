package com.CocoCode.pickleballers.repository;

import com.CocoCode.pickleballers.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByIdempotencyKey(String key);

    @Query("""
        SELECT m FROM Match m
        WHERE m.status = :status
          AND ((m.playerA.id = :id1 AND m.playerB.id = :id2)
            OR (m.playerA.id = :id2 AND m.playerB.id = :id1))
    """)
    Optional<Match> findPendingMatchBetweenPlayers(
            @Param("status") Match.Status status,
            @Param("id1") Long playerId1,
            @Param("id2") Long playerId2
    );
}
