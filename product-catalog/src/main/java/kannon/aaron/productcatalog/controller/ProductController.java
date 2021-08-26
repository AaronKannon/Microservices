package kannon.aaron.productcatalog.controller;


import kannon.aaron.productcatalog.model.Product;
import kannon.aaron.productcatalog.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequestMapping(value = "/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @PostMapping
    Product create(@RequestBody Product product) {
        Resilience4JCircuitBreaker circuitBreaker = circuitBreakerFactory.create("product-catalog");
        Supplier<Product> productSupplier = () -> productRepository.save(product);
        return circuitBreaker.run(productSupplier, throwable -> handleErrorCreate());
        //return productRepository.save(product);
    }

    @GetMapping(value="/catalog/{id}")
    Optional<Product> findById(@PathVariable Integer id){
        Resilience4JCircuitBreaker circuitBreaker = circuitBreakerFactory.create("product-catalog");
        Supplier<Optional<Product>> productSupplier = () -> productRepository.findById(id);
        return Optional.of(circuitBreaker.run(productSupplier, throwable -> handleErrorFind()).get());
        //return productRepository.findById(id);
    }

    private Product handleErrorCreate() {
        Product error = new Product();
        error.setName("Error - Didn't Create");
        error.setAmount(0);
        return error;
    }

    private Optional<Product> handleErrorFind() {
        Optional<Product> error = Optional.of(new Product());
        error.get().setName("Error - Didn't Found");
        error.get().setAmount(0);
        return error;
    }

}
