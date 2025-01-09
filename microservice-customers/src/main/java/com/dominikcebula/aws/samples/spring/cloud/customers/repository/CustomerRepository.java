package com.dominikcebula.aws.samples.spring.cloud.customers.repository;

import com.dominikcebula.aws.samples.spring.cloud.customers.model.CustomerDTO;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends ListCrudRepository<CustomerDTO, Long> {
}
