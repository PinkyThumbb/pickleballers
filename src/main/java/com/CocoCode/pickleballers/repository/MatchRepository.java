package com.CocoCode.pickleballers.repository;

import com.CocoCode.pickleballers.entity.Match;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByIdempotencyKey(String key);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query("""
    select m from Match m
    where m.status in :statuses
    and (
        (m.playerA.id = :playerIdA and m.playerB.id = :playerIdB)
        or
        (m.playerA.id = :playerIdB and m.playerB.id = :playerIdA)
    )
""")
    Optional<Match> findPendingOrDisputedMatchBetweenPlayers(
            @Param("statuses") List<Match.Status> statuses,
            @Param("playerIdA") Long playerIdA,
            @Param("playerIdB") Long playerIdB
    );
}
