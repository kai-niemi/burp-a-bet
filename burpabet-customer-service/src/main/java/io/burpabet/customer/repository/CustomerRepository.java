package io.burpabet.customer.repository;

import io.burpabet.common.domain.Status;
import io.burpabet.customer.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @Query(value = "select c "
            + "from Customer c where c.status = ?1")
    Page<Customer> findAllWithStatus(Status status, Pageable pageable);
}
