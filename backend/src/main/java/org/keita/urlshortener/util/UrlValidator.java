package org.keita.urlshortener.util;

import java.net.URI;

/**
 * Простейшая утилита для валидации URL.
 * Здесь мы только проверяем корректность синтаксиса и наличие схемы (http/https).
 */
public class UrlValidator {

    /**
     * Проверяет, что строка является корректным URL (http или https).
     *
     * @param value строка, которую нужно проверить
     * @return true, если URL выглядит корректным
     */
    public static boolean isValidHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null) {
                return false;
            }
            String scheme = uri.getScheme().toLowerCase();
            return scheme.equals("http") || scheme.equals("https");
        } catch (Exception e) {
            return false;
        }
    }
}
