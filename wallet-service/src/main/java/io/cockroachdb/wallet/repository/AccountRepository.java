package io.cockroachdb.wallet.repository;

import io.cockroachdb.wallet.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    @Query(value = "select a "
            + "from Account a "
            + "where a.id in (?1)")
    @Lock(LockModeType.PESSIMISTIC_READ)
    List<Account> findAllByIdForUpdate(Set<UUID> ids);
}
