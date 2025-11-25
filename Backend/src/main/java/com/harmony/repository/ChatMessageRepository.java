package com.harmony.repository;

import com.harmony.entity.ChatMessage;
import com.harmony.repository.querydsl.DslChatMessageRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>,
    DslChatMessageRepository {
}
