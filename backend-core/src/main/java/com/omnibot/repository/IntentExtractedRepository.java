package com.omnibot.repository;

import com.omnibot.model.IntentExtracted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntentExtractedRepository extends JpaRepository<IntentExtracted, Long> {
    List<IntentExtracted> findByMessageId(Long messageId);
}
