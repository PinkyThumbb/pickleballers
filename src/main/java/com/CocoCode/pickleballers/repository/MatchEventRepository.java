package com.CocoCode.pickleballers.repository;

import com.CocoCode.pickleballers.entity.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
    List<MatchEvent> findByMatchIdOrderByCreatedAtAsc(Long matchId);
}
