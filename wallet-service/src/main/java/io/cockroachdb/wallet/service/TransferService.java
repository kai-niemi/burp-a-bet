package io.cockroachdb.wallet.service;

import io.cockroachdb.wallet.model.Transaction;

public interface TransferService {
    Transaction submitTransferRequest(TransferRequest transferRequest);
}
