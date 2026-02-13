package com.CocoCode.pickleballers.controller;

import com.CocoCode.pickleballers.BaseIT;
import com.CocoCode.pickleballers.entity.Player;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Sql(scripts = {"classpath:init.sql", "classpath:data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PlayerControllerIT extends BaseIT {

    @Test
    void getPlayers_returnsSeededPlayers() throws IOException {
        wireMockServer.stubFor(get(urlPathEqualTo("/players/getPlayers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":1,\"name\":\"Alice Johnson\",\"email\":\"idk\"}]")));

        Response response = given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .get("/players/getPlayers");

        assertEquals(200, response.getStatusCode());
        List<Player> players = response.as(new TypeRef<>() {});
        assertNotNull(players);
        assertEquals(1, players.size());
        assertTrue(players.stream().anyMatch(p -> "Alice Johnson".equals(p.getName())));
    }
}
