package org.keita.urlshortener.controller;

import jakarta.validation.Valid;
import org.keita.urlshortener.dto.CreateShortLinkRequest;
import org.keita.urlshortener.dto.CreateShortLinkResponse;
import org.keita.urlshortener.model.ShortLink;
import org.keita.urlshortener.service.ShortLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST-контроллер для работы с сокращёнными ссылками.
 * Все методы отдают и принимают JSON.
 */
@RestController
@RequestMapping("/api/links")
@CrossOrigin(origins = "*")
public class ApiShortLinkController {

    private final ShortLinkService shortLinkService;

    // Простая защита от спама: запоминаем время последнего запроса по IP.
    private static final long RATE_LIMIT_MILLIS = 5000; // 5 секунд между запросами
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();

    public ApiShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    /**
     * Создать новую сокращённую ссылку.
     */
    @PostMapping
    public ResponseEntity<?> createShortLink(
            @Valid @RequestBody CreateShortLinkRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            // --- Антиспам: простой лимит 1 запрос раз в 5 секунд с одного IP ---
            String clientIp = httpRequest.getRemoteAddr();
            long now = System.currentTimeMillis();
            Long last = lastRequestTimes.get(clientIp);
            if (last != null && now - last < RATE_LIMIT_MILLIS) {
                long remainingSec = Math.max(1, (RATE_LIMIT_MILLIS - (now - last)) / 1000);
                String message = "Слишком частые запросы. Попробуйте снова через " + remainingSec + " сек.";
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(message);
            }
            lastRequestTimes.put(clientIp, now);
            // -------------------------------------------------------------------

            ShortLink link = shortLinkService.createShortLink(request);
            String shortUrl = shortLinkService.buildShortUrl(link.getShortCode());
            String qrBase64 = shortLinkService.generateQrCodeBase64(shortUrl);

            CreateShortLinkResponse response = new CreateShortLinkResponse(
                    shortUrl,
                    link.getOriginalUrl(),
                    qrBase64
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Внутренняя ошибка сервера");
        }
    }
}
