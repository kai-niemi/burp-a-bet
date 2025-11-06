package io.cockroachdb.wallet.service;

/**
 * Business exception thrown if an account has insufficient funds.
 */
public class NegativeBalanceException extends BadRequestException {
    public NegativeBalanceException(String accountName) {
        super("Insufficient funds for '" + accountName + "'");
    }
}
