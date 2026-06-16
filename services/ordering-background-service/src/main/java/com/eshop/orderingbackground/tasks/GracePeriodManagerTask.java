package com.eshop.orderingbackground.tasks;

import com.eshop.eventbus.IEventBus;
import com.eshop.orderingbackground.integrationevents.events.GracePeriodConfirmedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Scheduled task that detects orders whose grace period has expired and
 * publishes {@link GracePeriodConfirmedIntegrationEvent} for each one.
 *
 * <p>The grace period is a configurable window (default 30 seconds) during
 * which a buyer may cancel an order after it has been submitted. Once the
 * grace period elapses, this task publishes the confirmation event so that
 * the ordering service can advance the order to {@code awaitingvalidation}.
 *
 * <p>This task uses {@link JdbcTemplate} directly — no JPA entities or
 * Flyway migrations are managed here; the database schema is owned by the
 * ordering service.
 */
@Component
@Slf4j
public class GracePeriodManagerTask {

    private final JdbcTemplate jdbcTemplate;
    private final IEventBus eventBus;
    private final long gracePeriodMs;

    public GracePeriodManagerTask(
            JdbcTemplate jdbcTemplate,
            IEventBus eventBus,
            @Value("${ordering.grace-period-ms:30000}") long gracePeriodMs) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventBus = eventBus;
        this.gracePeriodMs = gracePeriodMs;
    }

    /**
     * Polls the database for orders in {@code submitted} status (statusId = 1)
     * whose {@code OrderDate} is older than the configured grace period, and
     * publishes a {@link GracePeriodConfirmedIntegrationEvent} for each such order.
     *
     * <p>Runs on a fixed delay controlled by {@code ordering.check-update-time}
     * (default 30 000 ms). The delay starts after the previous execution completes,
     * so there is no overlap between runs.
     *
     * <p>This method is intentionally <em>not</em> {@code @Transactional}: it
     * only reads from the database and publishes events; no writes are made here.
     */
    @Scheduled(fixedDelayString = "${ordering.check-update-time:30000}")
    public void checkConfirmedGracePeriodOrders() {
        log.debug("Checking for orders past grace period (gracePeriodMs={})", gracePeriodMs);

        Instant cutoff = Instant.now().minusMillis(gracePeriodMs);
        Timestamp cutoffTimestamp = Timestamp.from(cutoff);

        String sql = "SELECT Id FROM ordering.orders WHERE OrderStatusId = 1 AND OrderDate < ?";

        List<Integer> orderIds = jdbcTemplate.queryForList(sql, Integer.class, cutoffTimestamp);

        if (orderIds.isEmpty()) {
            log.debug("No orders found past grace period cutoff {}", cutoff);
            return;
        }

        log.info("Found {} order(s) past grace period — publishing GracePeriodConfirmedIntegrationEvent", orderIds.size());

        for (Integer orderId : orderIds) {
            log.info("Publishing GracePeriodConfirmedIntegrationEvent for orderId={}", orderId);
            eventBus.publish(new GracePeriodConfirmedIntegrationEvent(orderId));
        }
    }
}
