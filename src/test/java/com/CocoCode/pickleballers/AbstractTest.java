package com.CocoCode.pickleballers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractTest {

    protected static ObjectMapper objectMapper;

    protected String readStringFromFile(String filename) throws IOException {
        return Files.readString(Path.of("src/test/resources/" + filename));
    }
}
