package com.tarantulacommunity.tarantulasocial.features.message.entity;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "thread_participants")
@Getter
@Setter
@NoArgsConstructor
public class ThreadParticipantEntity {

    @EmbeddedId
    private ThreadParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("threadId")
    @JoinColumn(name = "thread_id")
    private ThreadEntity thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private LocalDateTime lastReadAt;

    public ThreadParticipantEntity(ThreadEntity thread, UserEntity user, LocalDateTime lastReadAt) {
        this.thread = thread;
        this.user = user;
        this.id = new ThreadParticipantId(thread.getId(), user.getId());
        this.lastReadAt = lastReadAt;
    }
}
