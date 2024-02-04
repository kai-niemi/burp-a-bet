package io.burpabet.wallet.repository;

import io.burpabet.wallet.model.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, TransactionItem.Id> {
}
