package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Exchange;
import com.speakBuddy.speackBuddy_backend.models.ExchangeJoinRequest;
import com.speakBuddy.speackBuddy_backend.models.ExchangeJoinRequestStatus;
import com.speakBuddy.speackBuddy_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeJoinRequestRepository extends JpaRepository<ExchangeJoinRequest, Long> {

    Optional<ExchangeJoinRequest> findByExchangeAndUserAndStatus(
            Exchange exchange, User user, ExchangeJoinRequestStatus status);

    List<ExchangeJoinRequest> findByExchangeIdAndStatus(Long exchangeId, ExchangeJoinRequestStatus status);

    boolean existsByExchangeAndUserAndStatus(Exchange exchange, User user, ExchangeJoinRequestStatus status);

    boolean existsByExchangeAndUser(Exchange exchange, User user);
}
