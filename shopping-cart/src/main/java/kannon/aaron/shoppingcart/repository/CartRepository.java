package kannon.aaron.shoppingcart.repository;

import kannon.aaron.shoppingcart.entity.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, Integer> {
}
