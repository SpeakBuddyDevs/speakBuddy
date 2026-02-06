package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Exchange;
import com.speakBuddy.speackBuddy_backend.models.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long>, JpaSpecificationExecutor<Exchange> {

    List<Exchange> findByStatus(ExchangeStatus status);
}
