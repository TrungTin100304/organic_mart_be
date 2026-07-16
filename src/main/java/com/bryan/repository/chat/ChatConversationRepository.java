package com.bryan.repository.chat;

import com.bryan.entity.chat.ChatConversation;
import com.bryan.entity.chat.ChatConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    @Query("SELECT c FROM ChatConversation c JOIN FETCH c.user WHERE c.user.id = :userId AND c.status = :status ORDER BY c.updatedAt DESC")
    Optional<ChatConversation> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") ChatConversationStatus status
    );

    @Query("SELECT c FROM ChatConversation c JOIN FETCH c.user WHERE c.user.id = :userId ORDER BY c.updatedAt DESC")
    Optional<ChatConversation> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT c FROM ChatConversation c JOIN FETCH c.user WHERE c.id = :id")
    Optional<ChatConversation> findByIdWithUser(@Param("id") Long id);

    Page<ChatConversation> findByStatusOrderByUpdatedAtDesc(ChatConversationStatus status, Pageable pageable);

    @Query("SELECT c FROM ChatConversation c JOIN FETCH c.user ORDER BY c.updatedAt DESC")
    Page<ChatConversation> findAllOrderByUpdatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(c) FROM ChatConversation c WHERE c.status = :status")
    long countByStatus(@Param("status") ChatConversationStatus status);
}
