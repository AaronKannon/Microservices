package kannon.aaron.shoppingcart.controller;

import kannon.aaron.shoppingcart.entity.Cart;
import kannon.aaron.shoppingcart.entity.Item;
import kannon.aaron.shoppingcart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequestMapping(value = "/cart")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @PostMapping(value = "/{id}")
    public Cart addItem(@PathVariable("id") Integer id, @RequestBody Item item){
        Resilience4JCircuitBreaker circuitBreaker = circuitBreakerFactory.create("shopping-cart");
        Optional<Cart> savedCart = findById(id);
        Cart cart;
        if(savedCart.equals(Optional.empty())){
            cart = new Cart(id);
        }else{
            cart = savedCart.get();
        }
        boolean check = checkAlreadyExistsItem(cart.getItems().stream().iterator(),item);
        if (!check) {
            cart.getItems().add(item);
        }
        Supplier<Cart> cartSupplier = () -> cartRepository.save(cart);
        return circuitBreaker.run(cartSupplier, throwable -> handleCartErrorAddItem());
        //return cartRepository.save(cart);
    }

    @GetMapping(value = "/{id}")
    public Optional<Cart> findById(@PathVariable("id") Integer id){
        Resilience4JCircuitBreaker circuitBreaker = circuitBreakerFactory.create("shopping-cart");
        Supplier<Optional<Cart>> cartSupplier = () -> cartRepository.findById(id);
        return circuitBreaker.run(cartSupplier, throwable -> handleCartErrorFind());
        //return cartRepository.findById(id);
    }

    @DeleteMapping(value = "/{id}")
    public void clear(@PathVariable("id") Integer id){
        cartRepository.deleteById(id);
    }

    private boolean checkAlreadyExistsItem(Iterator<Item> iterator, Item item) {
        boolean result = false;
        while (iterator.hasNext()) {
            Item check = iterator.next();
            if(check.getProductId().equals(item.getProductId())){
                int add = check.getAmount();
                add++;
                check.setAmount(add);
                result = true;
            }
        }
        return result;
    }

    private Cart handleCartErrorAddItem() {
        return null;
    }

    private Optional<Cart> handleCartErrorFind() {
        return null;
    }
}
