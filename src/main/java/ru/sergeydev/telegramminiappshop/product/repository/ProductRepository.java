package ru.sergeydev.telegramminiappshop.product.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sergeydev.telegramminiappshop.product.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


    // Все активные товары для каталога
    List<Product> findByActiveTrueOrderBySortOrderAsc();

    // Один активный товар по id
    Optional<Product> findByIdAndActiveTrue(Long id);

    // Рекомендуемые активные товары
    List<Product> findByActiveTrueAndFeaturedTrueOrderBySortOrderAsc();

    // Активные товары конкретной категории
    List<Product> findByCategoryIdAndActiveTrueOrderBySortOrderAsc(Long categoryId);

    //для оформления заказа с блокировкой;
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Product p
            where p.id = :id
              and p.active = true
            """)
    Optional<Product> findActiveByIdForUpdate(@Param("id") Long id);


    //для возврата остатка при отмене.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Product p
            where p.id = :id
            """)
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    List<Product> findAllByOrderBySortOrderAsc();

    boolean existsByCategory_Id(Long categoryId);

    //запрос для проверки перед удалением существует ли товар в заказе
    @Query("""
            select case when count(item) > 0 then true else false end
            from OrderItem item
            where item.product.id = :productId
            """)
    boolean existsInOrders(@Param("productId") Long productId);

    @Query("""
            select distinct item.order.id
                    from OrderItem item
                    where item.product.id = :productId
            """)
    List<Long> findByOrderId(@Param("productId") Long productId);
}
