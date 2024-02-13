package io.burpabet.customer.repository;

import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.domain.Status;
import io.burpabet.customer.model.Customer;
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
}
