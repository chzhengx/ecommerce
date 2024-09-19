package com.mimo.ecommerce.business.repository;

import com.mimo.ecommerce.business.model.Order;
import com.mimo.ecommerce.business.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zcl on 2024/9/9.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * 根据用户查找其订单
     */
    List<Order> findByUser(User user);
}

