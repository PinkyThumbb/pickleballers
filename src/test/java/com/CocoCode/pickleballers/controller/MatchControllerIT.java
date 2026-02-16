package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.BaseIT;
import com.CocoCode.pickleballers.dto.CreateMatchResponseDTO;
import com.CocoCode.pickleballers.entity.Match;
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

@Sql(scripts = {"classpath:truncate.sql","classpath:seed.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MatchControllerIT extends BaseIT {

    @Test
    void getMatches_returnsMatches() {
        //ARRANGE AND ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/matches/getMatches");

        //ASSERT
        assertEquals(200, response.getStatusCode());
        List<Match> matches = response.as(new TypeRef<>() {});
        assertNotNull(matches);
        assertEquals(1, matches.size());
    }

    @Test
    void createNewMatch_returnsMatch() throws IOException {
        //ARRANGE AND ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createPendingMatch.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        //ASSERT
        assertEquals(201, response.getStatusCode());
        CreateMatchResponseDTO created = response.as(CreateMatchResponseDTO.class);
        assertNotNull(created);
        assertEquals(2, created.getId());
        assertEquals(2, created.getPlayerAId());
        assertEquals(3, created.getPlayerBId());
        assertEquals("11-9", created.getScore());
        assertEquals(Match.Status.PENDING, created.getStatus());
        assertEquals("a8252246-1764-4583-ab31-470ccdfe3d7f", created.getIdempotencyKey());
    }
}

