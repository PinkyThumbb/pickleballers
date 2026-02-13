package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.BaseIT;
import com.CocoCode.pickleballers.entity.Player;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Sql(scripts = {"classpath:init.sql", "classpath:data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PlayerControllerIT extends BaseIT {

    @Test
    void getPlayers_returnsSeededPlayers() {
        //ARRANGE AND ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/players/getPlayers");

        //ASSERT
        assertEquals(200, response.getStatusCode());
        List<Player> players = response.as(new TypeRef<>() {});
        assertNotNull(players);
        assertEquals(3, players.size());
    }

    @Test
    void createPlayers_returnsSeededPlayers() throws IOException {
        //ARRANGE AND ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createPlayer.json"))
                .post("/players/createPlayer");

        //ASSERT
        assertEquals(200, response.getStatusCode());
        Player created = response.as(Player.class);
        assertNotNull(created);
        assertEquals("Jacob", created.getName());
        assertEquals("JacobWinnifer@gmail.com", created.getEmail());
    }
}
