package org.keita.urlshortener.controller;

import org.keita.urlshortener.model.ShortLink;
import org.keita.urlshortener.service.ShortLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

/**
 * Контроллер, который отвечает за переход по коротким ссылкам.
 * При запросе вида GET /{code} мы ищем ссылку в Redis и делаем redirect.
 */
@Controller
public class RedirectController {

    private final ShortLinkService shortLinkService;

    public RedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/{code}")
    public String redirect(@PathVariable String code) {
        ShortLink link = shortLinkService.resolveAndIncrement(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ссылка не найдена"));

        // Возвращаем redirect через префикс "redirect:"
        return "redirect:" + link.getOriginalUrl();
    }
}
