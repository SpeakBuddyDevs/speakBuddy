package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.UserStatsDTO;
import com.speakBuddy.speackBuddy_backend.models.ExchangeParticipant;
import com.speakBuddy.speackBuddy_backend.models.ExchangeStatus;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Servicio de estadísticas de usuario.
 *
 * Calcula, a partir de las participaciones confirmadas del usuario, los
 * intercambios completados por mes y las horas por semana.
 *
 * Semana: de lunes 00:00 a domingo 23:59 (inclusive).
 */
@Service
public class StatsService {

    private final ExchangeParticipantRepository participantRepository;

    public StatsService(ExchangeParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    /**
     * Devuelve las estadísticas agregadas del usuario autenticado para Home.
     */
    public UserStatsDTO getUserStats(User user) {
        // Obtenemos todas las participaciones confirmadas en intercambios COMPLETED
        List<ExchangeParticipant> participations =
                participantRepository.findByUserAndConfirmedIsTrueAndExchange_Status(
                        user, ExchangeStatus.COMPLETED);

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Semana: lunes 00:00 → domingo 23:59
        LocalDate today = LocalDate.now();
        LocalDate mondayThisWeek = today.with(DayOfWeek.MONDAY);
        LocalDate mondayLastWeek = mondayThisWeek.minusWeeks(1);

        LocalDateTime startOfCurrentWeek = mondayThisWeek.atStartOfDay(); // lunes 00:00
        LocalDateTime endOfCurrentWeek = mondayThisWeek.plusDays(6).atTime(LocalTime.MAX); // domingo 23:59:59.999...
        LocalDateTime startOfLastWeek = mondayLastWeek.atStartOfDay();
        LocalDateTime endOfLastWeek = mondayLastWeek.plusDays(6).atTime(LocalTime.MAX);

        int exchangesThisMonth = 0;
        int exchangesLastMonth = 0;
        int minutesThisWeek = 0;
        int minutesLastWeek = 0;

        for (ExchangeParticipant p : participations) {
            LocalDateTime completedAt = p.getConfirmedAt();
            if (completedAt == null && p.getExchange() != null) {
                completedAt = p.getExchange().getScheduledAt();
            }
            if (completedAt == null) {
                continue;
            }

            YearMonth ym = YearMonth.from(completedAt);
            if (ym.equals(currentMonth)) {
                exchangesThisMonth++;
            } else if (ym.equals(previousMonth)) {
                exchangesLastMonth++;
            }

            int durationMinutes = 0;
            if (p.getExchange() != null && p.getExchange().getDurationMinutes() != null) {
                durationMinutes = p.getExchange().getDurationMinutes();
            }

            // Semana actual: lunes 00:00 ≤ completedAt ≤ domingo 23:59
            if (!completedAt.isBefore(startOfCurrentWeek) && !completedAt.isAfter(endOfCurrentWeek)) {
                minutesThisWeek += durationMinutes;
            }
            // Semana anterior: mismo intervalo la semana pasada
            else if (!completedAt.isBefore(startOfLastWeek) && !completedAt.isAfter(endOfLastWeek)) {
                minutesLastWeek += durationMinutes;
            }
        }

        double hoursThisWeek = minutesThisWeek / 60.0;
        double hoursLastWeek = minutesLastWeek / 60.0;

        return UserStatsDTO.builder()
                .exchangesThisMonth(exchangesThisMonth)
                .exchangesLastMonth(exchangesLastMonth)
                .hoursThisWeek(hoursThisWeek)
                .hoursLastWeek(hoursLastWeek)
                .build();
    }
}

