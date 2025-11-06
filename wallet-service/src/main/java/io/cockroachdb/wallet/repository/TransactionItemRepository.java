package io.cockroachdb.wallet.repository;

import io.cockroachdb.wallet.model.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, TransactionItem.Id> {
}
