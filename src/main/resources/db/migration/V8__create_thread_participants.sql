CREATE TABLE thread_participants (
                                     thread_id BIGINT NOT NULL REFERENCES threads(id) ON DELETE CASCADE,
                                     user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                     last_read_at TIMESTAMP,

                                     PRIMARY KEY (thread_id, user_id)
);

CREATE INDEX idx_thread_participants_user_id ON thread_participants (user_id);
