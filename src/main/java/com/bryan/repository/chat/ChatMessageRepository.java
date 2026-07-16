package com.bryan.repository.chat;

import com.bryan.entity.chat.ChatMessage;
import com.bryan.entity.chat.ChatMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    Optional<ChatMessage> findByConversationIdAndClientMessageId(Long conversationId, String clientMessageId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = :status WHERE m.conversation.id = :conversationId AND m.senderRole != 'USER' AND m.status = 'SENT'")
    int markAdminMessagesAsRead(@Param("conversationId") Long conversationId, @Param("status") ChatMessageStatus status);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = :status WHERE m.conversation.id = :conversationId AND m.senderRole != 'ADMIN' AND m.status = 'SENT'")
    int markUserMessagesAsRead(@Param("conversationId") Long conversationId, @Param("status") ChatMessageStatus status);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderRole != :role AND m.status = 'SENT'")
    long countUnreadByConversationAndRole(@Param("conversationId") Long conversationId, @Param("role") String role);

    boolean existsByConversationIdAndClientMessageId(Long conversationId, String clientMessageId);
}
