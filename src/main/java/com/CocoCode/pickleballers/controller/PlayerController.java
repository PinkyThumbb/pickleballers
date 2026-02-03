package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.entity.Player;
import com.CocoCode.pickleballers.repository.PlayerRepository;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Data
@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerRepository repository;

    @GetMapping("/getPlayers")
    public List<Player> all() {
        return repository.findAll();
    }

    @PostMapping("/createPlayer")
    public Player create(@RequestBody Player player) {
        return repository.save(player);
    }
}

