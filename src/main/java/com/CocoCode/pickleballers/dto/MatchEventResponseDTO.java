package com.CocoCode.pickleballers.dto;

import com.CocoCode.pickleballers.entity.MatchEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record MatchEventResponseDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("matchId") Long matchId,
        @JsonProperty("eventType") MatchEvent.EventType eventType,
        @JsonProperty("triggeredByKey") String triggeredByKey,
        @JsonProperty("score") String score,
        @JsonProperty("createdAt") LocalDateTime createdAt
) {
    public static MatchEventResponseDTO from(MatchEvent event) {
        return new MatchEventResponseDTO(
                event.getId(),
                event.getMatch().getId(),
                event.getEventType(),
                event.getTriggeredByKey(),
                event.getScore(),
                event.getCreatedAt()
        );
    }
}
