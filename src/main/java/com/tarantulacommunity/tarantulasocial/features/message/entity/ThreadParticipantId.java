package com.tarantulacommunity.tarantulasocial.features.message.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ThreadParticipantId implements Serializable {

    @Column(name = "thread_id")
    private Long threadId;

    @Column(name = "user_id")
    private Long userId;
}
