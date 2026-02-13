package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Exchange;
import com.speakBuddy.speackBuddy_backend.models.ExchangeParticipant;
import com.speakBuddy.speackBuddy_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeParticipantRepository extends JpaRepository<ExchangeParticipant, Long> {

    List<ExchangeParticipant> findByUser(User user);

    List<ExchangeParticipant> findByExchange(Exchange exchange);

    Optional<ExchangeParticipant> findByExchangeAndUser(Exchange exchange, User user);

    boolean existsByExchangeAndUser(Exchange exchange, User user);
}
