package io.cockroachdb.wallet.saga;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cockroachdb.betting.common.annotations.OutboxOperation;
import io.cockroachdb.betting.common.annotations.ServiceFacade;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;
import io.cockroachdb.betting.common.domain.BetPlacement;
import io.cockroachdb.betting.common.domain.BetSettlement;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.wallet.model.CustomerAccount;
import io.cockroachdb.wallet.model.OperatorAccount;
import io.cockroachdb.wallet.service.AccountService;
import io.cockroachdb.wallet.service.NoSuchAccountException;
import io.cockroachdb.wallet.service.TransferRequest;
import io.cockroachdb.wallet.service.TransferService;

@ServiceFacade
public class WalletBettingFacade {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    @TransactionBoundary
    @OutboxOperation(aggregateType = "placement")
    public BetPlacement reserveWager(BetPlacement placement) {
        Optional<CustomerAccount> optional = accountService
                .findCustomerAccountByForeignId(placement.getCustomerId());

        placement.setOrigin("wallet-service");

        if (optional.isEmpty()) {
            placement.setJurisdiction(placement.getJurisdiction());
            placement.setStatus(Status.REJECTED);
            placement.setStatusDetail("No such customer account: " + placement.getCustomerId());
            logger.warn("Bet placement rejected (no customer account): {}", placement);
            return placement;
        }

        CustomerAccount customerAccount = optional.get();

        if (customerAccount.getBalance().minus(placement.getStake()).isNegative()) {
            placement.setJurisdiction(customerAccount.getJurisdiction());
            placement.setStatus(Status.REJECTED);
            placement.setStatusDetail("Insufficient funds: " + customerAccount.getBalance());
            logger.warn("Bet placement rejected (insufficient funds): {}", placement);
            return placement;
        }

        OperatorAccount operatorAccount = accountService
                .findOperatorAccountById(customerAccount.getOperatorId())
                .orElseThrow(() -> new NoSuchAccountException(customerAccount.getOperatorId()));

        TransferRequest transferRequest = TransferRequest.builder()
                .withId(placement.getEventId())
                .withJurisdiction(customerAccount.getJurisdiction())
                .withTransactionType("bet-wager")
                .withBookingDate(LocalDate.now())
                .withTransferDate(LocalDate.now())
                .addLeg()
                .withId(customerAccount.getId())
                .withAmount(placement.getStake().negate())
                .withNote("Bet wager")
                .then()
                .addLeg()
                .withId(operatorAccount.getId())
                .withAmount(placement.getStake())
                .withNote("Bet wager by " + customerAccount.getName())
                .then()
                .build();

        transferService.submitTransferRequest(transferRequest);

        placement.setStatus(Status.APPROVED);
        placement.setJurisdiction(customerAccount.getJurisdiction());
        placement.setStatusDetail("Bet wager withdrawn");
        placement.setOrigin("wallet-service");

        logger.info("Bet placement approved: {}", placement);

        return placement;
    }

    @TransactionBoundary
    public BetPlacement reverseWager(BetPlacement placement) {
        CustomerAccount customerAccount = accountService
                .findCustomerAccountByForeignId(placement.getCustomerId())
                .orElseThrow(() -> new NoSuchAccountException(placement.getCustomerId()));

        OperatorAccount operatorAccount = accountService
                .findOperatorAccountById(customerAccount.getOperatorId())
                .orElseThrow(() -> new NoSuchAccountException(customerAccount.getOperatorId()));

        if (!"wallet-service".equals(placement.getOrigin())) {
            TransferRequest transferRequest = TransferRequest.builder()
                    .withId(placement.getEventId())
                    .withJurisdiction(customerAccount.getJurisdiction())
                    .withTransactionType("bet-wager-reversal")
                    .withBookingDate(LocalDate.now())
                    .withTransferDate(LocalDate.now())
                    .addLeg()
                    .withId(customerAccount.getId())
                    .withAmount(placement.getStake())
                    .withNote("Bet wager reversal")
                    .then()
                    .addLeg()
                    .withId(operatorAccount.getId())
                    .withAmount(placement.getStake().negate())
                    .withNote("Bet wager reversal for " + customerAccount.getName())
                    .then()
                    .build();

            transferService.submitTransferRequest(transferRequest);

            placement.setStatusDetail("Bet wager reversed");
        } else {
            placement.setStatusDetail("Bet wager not reversed (same origin)");
        }

        placement.setStatus(Status.APPROVED);
        placement.setOrigin("wallet-service");

        logger.warn("Bet placement reversal: {}", placement);

        return placement;
    }

    @TransactionBoundary
    @OutboxOperation(aggregateType = "settlement")
    public BetSettlement transferPayout(BetSettlement settlement) {
        if (settlement.getPayout().isPositive()) {
            CustomerAccount customerAccount = accountService
                    .findCustomerAccountByForeignId(settlement.getCustomerId())
                    .orElseThrow(() -> new NoSuchAccountException(settlement.getCustomerId()));

            OperatorAccount operatorAccount = accountService
                    .findOperatorAccountById(customerAccount.getOperatorId())
                    .orElseThrow(() -> new NoSuchAccountException(customerAccount.getOperatorId()));

            TransferRequest transferRequest = TransferRequest.builder()
                    .withId(settlement.getEventId())
                    .withJurisdiction(customerAccount.getJurisdiction())
                    .withTransactionType("bet-wager-payout")
                    .withBookingDate(LocalDate.now())
                    .withTransferDate(LocalDate.now())
                    .addLeg()
                    .withId(customerAccount.getId())
                    .withAmount(settlement.getPayout())
                    .withNote("Bet wager payout from " + operatorAccount.getName())
                    .then()
                    .addLeg()
                    .withId(operatorAccount.getId())
                    .withAmount(settlement.getPayout().negate())
                    .withNote("Bet win payout to " + customerAccount.getName())
                    .then()
                    .build();

            transferService.submitTransferRequest(transferRequest);

            settlement.setStatusDetail("Bet payout approved");
            logger.info("Bet payout approved: {}", settlement);
        } else {
            settlement.setStatusDetail("No bet payout");
            logger.info("Bet payout approved (no payout): {}", settlement);
        }

        settlement.setStatus(Status.APPROVED);
        settlement.setOrigin("wallet-service");

        return settlement;
    }
}
