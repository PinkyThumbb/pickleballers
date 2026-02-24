package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.BaseIT;
import com.CocoCode.pickleballers.dto.CreateMatchResponseDTO;
import com.CocoCode.pickleballers.dto.MatchEventResponseDTO;
import com.CocoCode.pickleballers.entity.Match;
import com.CocoCode.pickleballers.entity.MatchEvent;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

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
    void createNewMatch_returnsPendingMatch() throws IOException {
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

    @Test
    void createExistingMatch_returnsMatchConfirmed() throws IOException {
        //ARRANGE AND ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createConfirmedMatch.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        //ASSERT
        assertEquals(201, response.getStatusCode());
        CreateMatchResponseDTO created = response.as(CreateMatchResponseDTO.class);
        assertNotNull(created);
        assertEquals(1, created.getId());
        assertEquals(1, created.getPlayerAId());
        assertEquals(2, created.getPlayerBId());
        assertEquals("11-9", created.getScore());
        assertEquals(Match.Status.CONFIRMED, created.getStatus());
        assertEquals("a8252246-1764-4583-ab31-470ccdfe3d7d", created.getIdempotencyKey());
    }

    @Test
    void createDisputedExistingMatch_returnsMatchDisputed() throws IOException {
        //ARRANGE AND ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createDisputedMatch.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        //ASSERT
        assertEquals(201, response.getStatusCode());
        CreateMatchResponseDTO created = response.as(CreateMatchResponseDTO.class);
        assertNotNull(created);
        assertEquals(1, created.getId());
        assertEquals(1, created.getPlayerAId());
        assertEquals(2, created.getPlayerBId());
        assertEquals("11-9", created.getScore());
        assertEquals(Match.Status.DISPUTED, created.getStatus());
        assertEquals("a8252246-1764-4583-ab31-470ccdfe3d7d", created.getIdempotencyKey());
    }

    @Test
    void createDisputedExistingMatchInvalidScore_returnsError() throws IOException {
        //ARRANGE AND ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createDisputedMatchInvalidScore.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        //ASSERT
        assertEquals(400, response.getStatusCode());
        assertEquals("Winner must win by at least 2 points", response.jsonPath().getString("error"));
    }

    @Test
    void getMatchHistory_afterCreatePending_returnsCreatedEvent() throws IOException {
        // ARRANGE
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createPendingMatch.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        // ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/matches/2/history");

        // ASSERT
        assertEquals(200, response.getStatusCode());
        List<MatchEventResponseDTO> events = response.as(new TypeRef<>() {});
        assertEquals(1, events.size());
        assertEquals(MatchEvent.EventType.CREATED, events.getFirst().eventType());
        assertEquals("a8252246-1764-4583-ab31-470ccdfe3d7f", events.getFirst().triggeredByKey());
    }

    @Test
    void getMatchHistory_forSeededMatch_returnsCreatedEvent() {
        // ACT - match 1 is pre-seeded, should already have a CREATED event from seed.sql
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/matches/1/history");

        // ASSERT
        assertEquals(200, response.getStatusCode());
        List<MatchEventResponseDTO> events = response.as(new TypeRef<>() {});
        assertEquals(1, events.size());
        assertEquals(MatchEvent.EventType.CREATED, events.getFirst().eventType());
        assertEquals(1L, events.getFirst().matchId());
    }

    @Test
    void getMatchHistory_afterConfirmed_returnsTwoEvents() throws IOException {
        // ARRANGE - resolve seeded pending match to CONFIRMED
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createConfirmedMatch.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        // ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/matches/1/history");

        // ASSERT
        assertEquals(200, response.getStatusCode());
        List<MatchEventResponseDTO> events = response.as(new TypeRef<>() {});
        assertEquals(2, events.size());
        assertEquals(MatchEvent.EventType.CREATED, events.get(0).eventType());
        assertEquals(MatchEvent.EventType.RESOLVED, events.get(1).eventType());
        assertEquals("a8252246-1764-4583-ab31-470ccdfe3d7f", events.get(1).triggeredByKey());
    }

    @Test
    void getMatchHistory_afterDisputed_returnsTwoEvents() throws IOException {
        // ARRANGE - resolve seeded pending match to DISPUTED
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createDisputedMatch.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        // ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/matches/1/history");

        // ASSERT
        assertEquals(200, response.getStatusCode());
        List<MatchEventResponseDTO> events = response.as(new TypeRef<>() {});
        assertEquals(2, events.size());
        assertEquals(MatchEvent.EventType.CREATED, events.get(0).eventType());
        assertEquals(MatchEvent.EventType.DISPUTED, events.get(1).eventType());
        assertEquals("a8252246-1764-4583-ab31-470ccdfe3d7f", events.get(1).triggeredByKey());
    }

    @Test
    void getMatchHistory_eventsOrderedByCreatedAt() throws IOException {
        // ARRANGE - trigger two events on match 1
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(readStringFromFile("json/createConfirmedMatch.json"))
                .header("Idempotency-Key", "a8252246-1764-4583-ab31-470ccdfe3d7f")
                .post("/matches/createMatch");

        // ACT
        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/matches/1/history");

        // ASSERT
        assertEquals(200, response.getStatusCode());
        List<MatchEventResponseDTO> events = response.as(new TypeRef<>() {});
        // verify ascending order
        for (int i = 0; i < events.size() - 1; i++) {
            assertNotNull(events.get(i).createdAt());
            assertFalse(events.get(i).createdAt().isAfter(events.get(i + 1).createdAt()), "Events should be ordered ascending by createdAt");
        }
    }
}

