package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Exchange;
import com.speakBuddy.speackBuddy_backend.models.ExchangeChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeChatMessageRepository extends JpaRepository<ExchangeChatMessage, Long> {

    List<ExchangeChatMessage> findByExchangeOrderByTimestampAsc(Exchange exchange);

    Page<ExchangeChatMessage> findByExchangeOrderByTimestampAsc(Exchange exchange, Pageable pageable);

    Optional<ExchangeChatMessage> findFirstByExchangeOrderByTimestampDesc(Exchange exchange);
}
