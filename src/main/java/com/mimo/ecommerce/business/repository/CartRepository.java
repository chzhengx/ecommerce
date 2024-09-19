package com.mimo.ecommerce.business.repository;

import com.mimo.ecommerce.business.model.Cart;
import com.mimo.ecommerce.business.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by zcl on 2024/9/9.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUser(User user);
}

