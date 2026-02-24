package com.CocoCode.pickleballers.helper;

import com.CocoCode.pickleballers.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PlayerServiceHelper {

    private final PlayerRepository playerRepository;

    public void validatePlayersExist(long a, long b) {
        if (!playerRepository.existsById(a)) throw new IllegalArgumentException("Player A not found");
        if (!playerRepository.existsById(b)) throw new IllegalArgumentException("Player B not found");
    }

}
