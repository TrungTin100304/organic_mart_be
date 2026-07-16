-- Chat Support System Tables
-- V32__chat_support.sql

-- Chat Conversations table
CREATE TABLE chat_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Chat Messages table
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    sender_id BIGINT REFERENCES users(id),
    sender_role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    client_message_id VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_chat_conversations_user_id ON chat_conversations(user_id);
CREATE INDEX idx_chat_conversations_status ON chat_conversations(status);
CREATE INDEX idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at);
CREATE UNIQUE INDEX idx_chat_messages_client_id ON chat_messages(conversation_id, client_message_id) WHERE client_message_id IS NOT NULL;

-- Add constraint for status values
ALTER TABLE chat_conversations ADD CONSTRAINT chk_conversation_status CHECK (status IN ('OPEN', 'CLOSED'));
ALTER TABLE chat_messages ADD CONSTRAINT chk_sender_role CHECK (sender_role IN ('USER', 'ADMIN', 'SYSTEM'));
ALTER TABLE chat_messages ADD CONSTRAINT chk_message_status CHECK (status IN ('SENT', 'READ'));
