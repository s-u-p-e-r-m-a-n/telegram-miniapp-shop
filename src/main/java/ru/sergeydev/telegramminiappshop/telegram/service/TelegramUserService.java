package ru.sergeydev.telegramminiappshop.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.telegram.entity.TelegramUserEntity;
import ru.sergeydev.telegramminiappshop.telegram.repository.TelegramUserRepository;

@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    @Transactional
    public TelegramUserEntity saveOrUpdate(
            Long telegramUserId,
            Long telegramChatId
    ) {
        TelegramUserEntity user = telegramUserRepository
                .findById(telegramUserId)
                .orElseGet(() -> TelegramUserEntity.builder()
                        .telegramUserId(telegramUserId)
                        .telegramChatId(telegramChatId)
                        .build());

        user.setTelegramChatId(telegramChatId);

        return telegramUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TelegramUserEntity getByTelegramUserId(Long telegramUserId) {
        return telegramUserRepository.findById(telegramUserId)
                .orElseThrow(() ->
                        new NotFoundException("Telegram-пользователь не найден")
                );
    }
}