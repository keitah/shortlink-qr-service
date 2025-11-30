package org.keita.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для входящего запроса на создание сокращённой ссылки.
 */
public class CreateShortLinkRequest {

    /**
     * Исходный URL, который нужно сократить.
     */
    @NotBlank(message = "URL не может быть пустым")
    @Size(max = 2048, message = "URL слишком длинный")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
