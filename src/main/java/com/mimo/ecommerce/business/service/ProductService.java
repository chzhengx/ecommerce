package com.mimo.ecommerce.business.service;

import com.mimo.ecommerce.business.model.Product;
import com.mimo.ecommerce.business.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zcl on 2024/9/9.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final RedisTemplate<String, Object> redisTemplate;

    public ProductService(ProductRepository productRepository, ElasticsearchOperations elasticsearchOperations, RedisTemplate<String, Object> redisTemplate) {
        this.productRepository = productRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.redisTemplate = redisTemplate;
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContaining(keyword, pageable);
    }

    @CacheEvict(value = "products", allEntries = true) // 清除缓存
    public Product updateProduct(Long productId, Product updatedProduct) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());
        productRepository.save(product);
        return product;
    }

    @Cacheable(value = "products", key = "#keyword")
    public List<Product> searchInElasticsearch(String keyword) {
        // 创建查询条件
        Criteria criteria = new Criteria("name").contains(keyword);
        CriteriaQuery query = new CriteriaQuery(criteria);

        // 使用 search 方法执行查询，获取 SearchHits
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);

        // 从 SearchHits 中提取出查询结果
        return searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void reduceStockWithRetry(Long productId, int quantity) {
        int retryCount = 3;
        while (retryCount > 0) {
            try {
                reduceStockWithLock(productId, quantity);
                return; // 成功，退出循环
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount--;
                if (retryCount == 0) {
                    throw new RuntimeException("Failed to reduce stock after retries");
                }
            }
        }
    }

    /**
     * 执行库存扣减操作，结合 Redis 分布式锁和乐观锁
     */
    @Transactional
    public void reduceStockWithLock(Long productId, int quantity) {
        String lockKey = "product_lock_" + productId;
        boolean lockAcquired = acquireLock(lockKey, 30); // 尝试获取锁，锁定 30 秒

        if (lockAcquired) {
            try {
                // 获取商品
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // 检查库存是否足够
                if (product.getStock() < quantity) {
                    throw new RuntimeException("Insufficient stock");
                }

                // 扣减库存
                product.setStock(product.getStock() - quantity);

                // 使用乐观锁保存，防止并发更新
                productRepository.save(product);

            } finally {
                // 确保锁被释放
                releaseLock(lockKey);
            }
        } else {
            throw new RuntimeException("Unable to acquire lock, try again later.");
        }
    }

    /**
     * 获取 Redis 分布式锁
     */
    private boolean acquireLock(String lockKey, long expireTimeInSeconds) {
        Duration expireDuration = Duration.ofSeconds(expireTimeInSeconds);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", expireDuration);
        return success != null && success;
    }

    /**
     * 释放 Redis 锁
     */
    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
}
