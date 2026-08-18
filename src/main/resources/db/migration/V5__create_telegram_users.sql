CREATE TABLE telegram_users (
                                telegram_user_id BIGINT PRIMARY KEY,
                                telegram_chat_id BIGINT NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);