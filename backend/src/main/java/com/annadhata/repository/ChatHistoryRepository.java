package com.annadhata.repository;

import com.annadhata.model.ChatHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {
    List<ChatHistory> findByUserIdOrderByTimestampAsc(String userId);
    List<ChatHistory> findTop50ByUserIdOrderByTimestampDesc(String userId);
}
