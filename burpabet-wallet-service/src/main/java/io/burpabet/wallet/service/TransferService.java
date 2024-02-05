package io.burpabet.wallet.service;

import io.burpabet.wallet.model.Transaction;

public interface TransferService {
    Transaction submitTransferRequest(TransferRequest transferRequest);
}
