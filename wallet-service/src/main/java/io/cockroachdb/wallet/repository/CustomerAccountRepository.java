package io.cockroachdb.wallet.repository;

import io.cockroachdb.wallet.model.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, UUID> {
    Optional<CustomerAccount> findByForeignId(UUID id);

    List<CustomerAccount> findAllByOperatorId(UUID id);
}
