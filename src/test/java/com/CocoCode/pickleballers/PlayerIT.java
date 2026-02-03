package com.CocoCode.pickleballers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerIT extends BaseIT {

//    @Autowired
//    private TestRestTemplate restTemplate;

    @Test
    void dockerTest() {
        GenericContainer<?> container = new GenericContainer<>("postgres:16")
                .withExposedPorts(5432);
        container.start();
        System.out.println("Host: " + container.getHost() + ", Port: " + container.getFirstMappedPort());
        container.stop();
    }


//    @Test
//    void testCreateAndGetPlayer() {
////        // 1️⃣ Create a player
////        Player player = new Player();
////        player.setName("Alice");
////        player.setEmail("alice@example.com");
////
////        ResponseEntity<Player> postResponse = restTemplate.postForEntity(
////                "/players", player, Player.class
////        );
////
////        assertThat(postResponse.getStatusCode().is2xxSuccessful()).isTrue();
////        Player created = postResponse.getBody();
////        assertThat(created).isNotNull();
////        assertThat(created.getId()).isNotNull();
////        assertThat(created.getName()).isEqualTo("Alice");
////
////        // 2️⃣ Get all players
////        ResponseEntity<Player[]> getResponse = restTemplate.getForEntity(
////                "/players", Player[].class
////        );
////
////        assertThat(getResponse.getStatusCode().is2xxSuccessful()).isTrue();
////        Player[] players = getResponse.getBody();
////        assertThat(players).isNotNull();
////        assertThat(players.length).isGreaterThanOrEqualTo(1);
//    }
}

