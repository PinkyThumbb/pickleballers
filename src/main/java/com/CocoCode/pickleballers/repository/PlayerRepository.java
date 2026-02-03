package com.CocoCode.pickleballers.repository;

import com.CocoCode.pickleballers.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}

