package io.burpabet.wallet.service;

import io.burpabet.wallet.model.Transaction;

public interface TransferService {
    void deleteAllInBatch();

    Transaction submitTransferRequest(TransferRequest transferRequest);
}
