package com.nextech.moadream.server.v1.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nextech.moadream.server.v1.domain.chat.entity.ChatSession;
import com.nextech.moadream.server.v1.domain.user.entity.User;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);

    List<ChatSession> findByUserAndIsActiveOrderByUpdatedAtDesc(User user, Boolean isActive);

    Optional<ChatSession> findBySessionIdAndUser(Long sessionId, User user);
}
