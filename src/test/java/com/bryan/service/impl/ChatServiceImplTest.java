package com.bryan.service.impl;

import com.bryan.entity.Role;
import com.bryan.entity.User;
import com.bryan.entity.chat.ChatConversation;
import com.bryan.entity.chat.ChatConversationStatus;
import com.bryan.exception.NotFoundException;
import com.bryan.repository.UserRepository;
import com.bryan.repository.chat.ChatConversationRepository;
import com.bryan.repository.chat.ChatMessageRepository;
import com.bryan.service.chat.impl.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void getOrCreateConversationMustUseWritableTransaction() throws NoSuchMethodException {
        Method method = ChatServiceImpl.class.getMethod("getOrCreateConversation", Long.class);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertFalse(transactional != null && transactional.readOnly());
    }

    @Test
    void getCurrentConversationReturnsExistingConversationWithoutCreatingOne() {
        User user = user(1L);
        ChatConversation conversation = ChatConversation.builder()
                .id(10L)
                .user(user)
                .status(ChatConversationStatus.OPEN)
                .build();

        when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()))
                .thenReturn(Optional.of(conversation));

        var response = chatService.getCurrentConversation(user.getId());

        assertEquals(conversation.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId());
        verify(conversationRepository, never()).save(any(ChatConversation.class));
    }

    @Test
    void getCurrentConversationThrowsNotFoundWithoutCreatingWhenConversationDoesNotExist() {
        Long userId = 1L;
        when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> chatService.getCurrentConversation(userId));
        verify(conversationRepository, never()).save(any(ChatConversation.class));
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("buyer@example.com");
        user.setFullName("Buyer");
        user.setRole(Role.ROLE_USER);
        return user;
    }
}
