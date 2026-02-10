package com.CocoCode.pickleballers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerIT extends BaseIT {

    @Test
    void dockerTest() {
        System.out.println("JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("DB Host: " + postgres.getHost() + ", Port: " + postgres.getFirstMappedPort());
        System.out.println("Container logs:\n" + postgres.getLogs());
        assertTrue(postgres.isRunning(), "Postgres container should be running");
    }
}

