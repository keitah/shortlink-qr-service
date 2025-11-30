package org.keita.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Модель сокращённой ссылки, которая хранится в Redis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortLink {

    /**
     * Короткий код, который будет использоваться в URL.
     */
    private String shortCode;

    /**
     * Исходный (оригинальный) URL.
     */
    private String originalUrl;

    /**
     * Время создания записи.
     */
    private LocalDateTime createdAt;

    /**
     * Количество переходов по короткой ссылке.
     */
    private long hitCount;
}
