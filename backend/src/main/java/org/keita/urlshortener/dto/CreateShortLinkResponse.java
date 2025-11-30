package org.keita.urlshortener.dto;

/**
 * DTO для ответа при создании сокращённой ссылки.
 */
public class CreateShortLinkResponse {

    /**
     * Короткая ссылка (полный URL вида http://host/{code}).
     */
    private String shortUrl;

    /**
     * Исходный URL.
     */
    private String originalUrl;

    /**
     * QR-код в виде Base64-строки (PNG).
     */
    private String qrCodeBase64;

    public CreateShortLinkResponse(String shortUrl, String originalUrl, String qrCodeBase64) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
}
