package com.tarantulacommunity.tarantulasocial.features.message.repository;

import java.time.LocalDateTime;

public interface ThreadReadState {
    Long getThreadId();
    LocalDateTime getLastReadAt();
}
