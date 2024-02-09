package io.burpabet.wallet.repository;

import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.wallet.model.OperatorAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OperatorAccountRepository extends JpaRepository<OperatorAccount, UUID> {
    @Query(value = "select a "
            + "from OperatorAccount a "
            + "where a.jurisdiction=?1")
    List<OperatorAccount> findAllAccountsByJurisdiction(Jurisdiction jurisdiction);

    @Query(value = "select a "
            + "from OperatorAccount a order by random()")
    Page<OperatorAccount> findAllByRandom(Pageable pageable);
}

