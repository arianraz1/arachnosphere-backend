package com.tarantulacommunity.tarantulasocial.features.message.dto.response;

import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private UserSummaryResponse sender;
    private UserSummaryResponse recipient;
    private String content;
    private LocalDateTime createdAt;
}