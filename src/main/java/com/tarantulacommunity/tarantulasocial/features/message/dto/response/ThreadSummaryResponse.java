package com.tarantulacommunity.tarantulasocial.features.message.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThreadSummaryResponse {

    private Long threadId;
    private String subject;
    private String otherParticipantUsername;
    private Long otherParticipantId;
    private String latestMessagePreview;
    private LocalDateTime latestMessageTimestamp;
    private boolean active;
}