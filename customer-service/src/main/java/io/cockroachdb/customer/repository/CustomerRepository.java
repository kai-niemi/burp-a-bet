package io.cockroachdb.customer.repository;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.customer.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);

    @Query(value = "select c "
            + "from Customer c where c.status = ?1")
    Page<Customer> findAllWithStatus(Status status, Pageable pageable);

    @Query(value = "select c "
            + "from Customer c where c.jurisdiction = ?1")
    Page<Customer> findAllWithJurisdiction(Jurisdiction jurisdiction, Pageable pageable);

    @Query(value = "select c "
            + "from Customer c "
            + "order by random() "
            + "limit 1")
    Optional<Customer> findAny();
}
