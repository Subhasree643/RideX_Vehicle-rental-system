package com.rental.service;

import com.rental.model.Customer;
import com.rental.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer registerCustomer(Customer customer) {
        if (customerRepository.existsByUsername(customer.getUsername())) {
            throw new RuntimeException("Username already exists: " + customer.getUsername());
        }
        customer.setRented(false);
        customer.setLoyaltyPoints(0);
        return customerRepository.save(customer);
    }

    public Customer login(String username, String password) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (!customer.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        return customer;
    }

    public List<Customer> getRentedCustomers() {
        return customerRepository.findByRentedTrue();
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
    public void deleteCustomer(Long id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Customer not found"));
    customerRepository.delete(customer);
}
}
