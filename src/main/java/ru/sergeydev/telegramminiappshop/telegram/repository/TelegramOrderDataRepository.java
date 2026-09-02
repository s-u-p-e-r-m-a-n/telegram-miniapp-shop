package ru.sergeydev.telegramminiappshop.telegram.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sergeydev.telegramminiappshop.telegram.entity.TelegramOrderDataEntity;

import java.util.List;
import java.util.Optional;

public interface TelegramOrderDataRepository
        extends JpaRepository<TelegramOrderDataEntity, Long> {
//найти все связи этого Telegram-пользователя и отсортировать их по дате связанных заказов — сначала новые.
    List<TelegramOrderDataEntity> findByTelegramUserTelegramUserIdOrderByOrderCreatedAtDesc(Long telegramUserId);

    //получаем все данные из репозитория и достаем только интересующее поле по orderId
    @Query("""
        select data.telegramUser.telegramChatId
        from TelegramOrderDataEntity data
        where data.orderId = :orderId
        """)
    Optional<Long> findTelegramChatIdByOrderId(
            @Param("orderId") Long orderId
    );
}