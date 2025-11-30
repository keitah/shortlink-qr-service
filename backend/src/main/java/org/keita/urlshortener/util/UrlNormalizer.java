package org.keita.urlshortener.util;

import java.net.IDN;
import java.net.URL;

/**
 * Утилита для нормализации URL.
 *
 * Возможности:
 *  1) Если схема (http/https) не указана — автоматически добавляем https://
 *  2) Если указана http — принудительно переводим в https
 *  3) Домен в unicode (кейта.рф) корректно обрабатывается через punycode
 */
public class UrlNormalizer {

    /**
     * Нормализует URL-строку.
     *
     * @param raw исходная строка, введённая пользователем
     * @return нормализованный URL или null, если строка некорректна
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }

        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            // Если схема не указана — добавляем https://
            if (!hasScheme(value)) {
                value = "https://" + value;
            }

            URL url = new URL(value);

            String protocol = url.getProtocol();
            if (protocol == null) {
                return null;
            }

            protocol = protocol.toLowerCase();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                // Разрешаем только http/https
                return null;
            }

            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return null;
            }

            // Преобразуем домен в punycode, если он был в unicode
            String asciiHost = IDN.toASCII(host);

            int port = url.getPort();  // -1 — порт по умолчанию
            String file = url.getFile(); // путь + query

            StringBuilder sb = new StringBuilder();
            sb.append("https://"); // Принудительно используем https
            sb.append(asciiHost);

            // Добавляем порт только если он нестандартный
            if (port != -1 && port != 80 && port != 443) {
                sb.append(":").append(port);
            }

            if (file != null && !file.isEmpty()) {
                if (!file.startsWith("/")) {
                    sb.append("/");
                }
                sb.append(file);
            }

            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Простейшая проверка наличия схемы вида "xxx:" в начале строки.
     */
    private static boolean hasScheme(String value) {
        return value.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*");
    }
}
