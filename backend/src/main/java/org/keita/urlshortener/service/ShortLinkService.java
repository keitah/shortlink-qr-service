package org.keita.urlshortener.service;

import lombok.RequiredArgsConstructor;
import org.keita.urlshortener.dto.CreateShortLinkRequest;
import org.keita.urlshortener.model.ShortLink;
import org.keita.urlshortener.util.QrCodeGenerator;
import org.keita.urlshortener.util.UrlNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

/**
 * Сервис, который отвечает за:
 *  - нормализацию URL (https по умолчанию, punycode для доменов на кириллице);
 *  - генерацию короткого кода;
 *  - сохранение и получение записей из Redis;
 *  - учёт количества переходов;
 *  - генерацию QR-кода.
 */
@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private static final String KEY_PREFIX = "shortlink:";
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    private final RedisTemplate<String, ShortLink> redisTemplate;

    /**
     * Базовый URL для коротких ссылок. Например: http://localhost:8080
     * Можно переопределить через переменную окружения BASE_URL.
     *
     * В проде здесь обычно будет https-домен,
     * но для локальной разработки удобно оставить http.
     */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final Random random = new Random();

    /**
     * Создаёт новую сокращённую ссылку.
     *
     * @param request DTO с исходным URL
     * @return объект ShortLink
     */
    @Transactional
    public ShortLink createShortLink(CreateShortLinkRequest request) {
        String rawUrl = request.getUrl();

        // Нормализуем URL:
        //  - добавляем https, если схемы нет;
        //  - конвертируем http -> https;
        //  - конвертируем unicode-домен в punycode.
        String normalizedUrl = UrlNormalizer.normalize(rawUrl);
        if (normalizedUrl == null) {
            throw new IllegalArgumentException("Некорректный URL. Введите, например, example.com или https://example.com");
        }

        String code = generateUniqueCode();
        ShortLink link = new ShortLink(
                code,
                normalizedUrl,
                LocalDateTime.now(),
                0L
        );

        String key = KEY_PREFIX + code;
        redisTemplate.opsForValue().set(key, link);

        return link;
    }

    /**
     * Находит ссылку по коду, увеличивает счётчик переходов и сохраняет обновление.
     *
     * @param code короткий код
     * @return Optional с найденной ссылкой
     */
    @Transactional
    public Optional<ShortLink> resolveAndIncrement(String code) {
        String key = KEY_PREFIX + code;
        ShortLink link = redisTemplate.opsForValue().get(key);
        if (link == null) {
            return Optional.empty();
        }

        link.setHitCount(link.getHitCount() + 1);
        redisTemplate.opsForValue().set(key, link);

        return Optional.of(link);
    }

    /**
     * Формирует полный короткий URL.
     *
     * @param code короткий код
     * @return строка вида http://host/{code}
     */
    
    public String buildShortUrl(String code) {
    String url = baseUrl.endsWith("/")
            ? baseUrl + code
            : baseUrl + "/" + code;

    // Для отображения пользователю обычно показываем https://,
    // но не трогаем http для localhost и 127.0.0.1.
    if (url.startsWith("http://")
            && !url.startsWith("http://localhost")
            && !url.startsWith("http://127.0.0.1")) {
        url = "https://" + url.substring(7);
    }
    return url;
}



    

    /**
     * Генерирует QR-код в PNG и возвращает его как base64-строку.
     *
     * @param shortUrl короткий URL, который кодируем
     * @return строка base64 (можно вставлять в data:image/png;base64,...)
     */
    public String generateQrCodeBase64(String shortUrl) {
        try {
            byte[] png = QrCodeGenerator.generatePng(shortUrl, 256);
            return Base64.getEncoder().encodeToString(png);
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка генерации QR-кода", ex);
        }
    }

/**
     * Генерирует уникальный короткий код, проверяя наличие в Redis.
     *
     * @return уникальный код
     */
    private String generateUniqueCode() {
        while (true) {
            String code = randomCode();
            String key = KEY_PREFIX + code;
            ShortLink existing = redisTemplate.opsForValue().get(key);
            if (existing == null) {
                return code;
            }
        }
    }

    /**
     * Генерирует случайный код из символов ALPHABET фиксированной длины.
     */
    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}
