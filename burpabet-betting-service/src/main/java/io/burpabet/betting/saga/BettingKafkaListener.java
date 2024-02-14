package io.burpabet.betting.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.burpabet.common.annotations.Retryable;
import io.burpabet.common.annotations.SagaStepAction;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.Registration;
import io.burpabet.common.domain.RegistrationEvent;
import io.burpabet.common.domain.Status;
import io.burpabet.common.domain.TopicNames;
import io.burpabet.common.outbox.OutboxRepository;

/**
 * Event listener for the customer registration journey (Saga).
 * <p>
 * The process steps for betting is to create a customer account and
 * grant a registration bonus amount from all operators in the
 * customer's region.
 * <p>
 * In the event of a Saga rollback, the bonus is reversed back to
 * each operator but the customer account remains in the system.
 */
@Component
@SagaStepAction(description = "Receives registration events and validates jurisdiction")
public class BettingKafkaListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OutboxRepository outboxRepository;

    @TransactionBoundary
    @Retryable
    @KafkaListener(id = "registration", topics = TopicNames.REGISTRATION, groupId = "betting",
            properties = {"spring.json.value.default.type=io.burpabet.common.domain.RegistrationEvent"})
    public void onRegistrationEvent(RegistrationEvent event) {
        Registration registration = event.getPayload();

        if (registration.getStatus().equals(Status.PENDING)) {
            registration.setStatus(Status.APPROVED);
            registration.setOrigin("betting-service");
            logger.info("Registration approved: {}", registration);
            outboxRepository.writeEvent(registration, "registration");
        } else {
            logger.info("Registration event received with status: %s".formatted(registration.getStatus()));
        }
    }
}
