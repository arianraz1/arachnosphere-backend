package com.tarantulacommunity.tarantulasocial.features.message.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateThreadRequest {

    @NotNull(message = "Recipient is required")
    private Long recipientId;

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content;

    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;
}