package ru.sergeydev.telegramminiappshop.telegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sergeydev.telegramminiappshop.telegram.entity.TelegramUserEntity;

public interface TelegramUserRepository extends JpaRepository<TelegramUserEntity,Long> {

}
