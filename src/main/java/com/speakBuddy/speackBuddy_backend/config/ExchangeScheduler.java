package com.speakBuddy.speackBuddy_backend.config;

import com.speakBuddy.speackBuddy_backend.service.ExchangeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Job que ejecuta periódicamente la transición de intercambios SCHEDULED
 * a ENDED_PENDING_CONFIRMATION cuando scheduled_at + duration ya ha pasado.
 */
@Component
public class ExchangeScheduler {

    private static final Logger logger = Logger.getLogger(ExchangeScheduler.class.getName());

    private final ExchangeService exchangeService;

    public ExchangeScheduler(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Scheduled(fixedRate = 300000) // cada 5 minutos
    public void processEndedExchanges() {
        int updated = exchangeService.processEndedExchanges();
        if (updated > 0) {
            logger.info("Intercambios pasados a ENDED_PENDING_CONFIRMATION: " + updated);
        }
    }
}
