package com.tungphongdo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tungphongdo.entity.Customer;
import com.tungphongdo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private static final String CUSTOMER_FOLDER_REDIS = "customer_info";

    private final CustomerRepository customerRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;


//    // ==================== Use Spring Cache Abstraction=============================
//    @Cacheable(value = "customers", key = "#id")   // Check cache first
//    public Optional<Customer> getCustomerById(Long id) {
//        // Get from MySQL if not in cache
//        return customerRepository.findById(id);
//    }
//
//    @CachePut(value = "customers", key = "#customer.id")  // Update cache and DB
//    public Customer updateCustomer(Customer customer) {
//        // Update in MySQL first
//        Customer updatedCustomer = customerRepository.save(customer);
//
//        // Cache will be updated with the updatedCustomer
//        return updatedCustomer;
//    }
//
//    @CacheEvict(value = "customers", key = "#id")  // Remove from cache after delete
//    public void deleteCustomer(Long id) {
//        // Remove from MySQL
//        customerRepository.deleteById(id);
//
//        // Invalidate cache by evicting the key
//    }
//
//    @Cacheable(value = "customerPage")
//    public Page<Customer> getAllCustomers(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return customerRepository.findAll(pageable);
//    }
//    // ==================== Use Spring Cache Abstraction=============================


    // ==================== Use Redis =============================
    public Customer storeCustomerInRedis(Customer customer) {
        // Save customer to MySQL first (source of truth)
        Customer customerSaved = customerRepository.save(customer);

        // Now update Redis with the same customer information
        try {
            String redisKey = String.format("%s:customer:%s", CUSTOMER_FOLDER_REDIS, customerSaved.getId());
            redisTemplate.opsForValue().set(redisKey, customer);
            redisTemplate.expire(redisKey, 1L, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return customerSaved;
    }

    public Customer getCustomerFromRedisOrMySQL(Long id) {
        String redisKey = String.format("%s:customer:%s", CUSTOMER_FOLDER_REDIS, id);
        Object rawCachedCustomer = redisTemplate.opsForValue().get(redisKey);
        if (Objects.nonNull(rawCachedCustomer)) {
            return objectMapper.convertValue(rawCachedCustomer, Customer.class);
        }

        // If not found in Redis, fetch from MySQL and update Redis
        Customer customer = customerRepository.findById(id).orElseThrow();
        redisTemplate.opsForValue().set(redisKey, customer);
        return customer;
    }
}
