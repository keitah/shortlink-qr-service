import React, { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";

interface CreateResponse {
  shortUrl: string;
  originalUrl: string;
  qrCodeBase64: string;
}

/**
 * Простой фронтенд на React, который общается с бэкендом:
 *  - поле для ввода URL (можно без http/https);
 *  - кнопка "Сократить";
 *  - вывод короткой ссылки и QR-кода;
 *  - переключатель светлой/тёмной темы.
 */
export const App: React.FC = () => {
  const [url, setUrl] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<CreateResponse | null>(null);
  // Встроенная защита от спама на фронтенде: запоминаем, когда можно отправить следующий запрос
  const [nextAllowedTime, setNextAllowedTime] = useState<number | null>(null);

  const [theme, setTheme] = useState<"light" | "dark">(() => {
    if (typeof window === "undefined") return "dark";
    const stored = window.localStorage.getItem("theme");
    return stored === "light" || stored === "dark" ? stored : "dark";
  });

  // При смене темы сохраняем её и добавляем класс на body
  useEffect(() => {
    document.body.classList.remove("theme-light", "theme-dark");
    document.body.classList.add(`theme-${theme}`);
    window.localStorage.setItem("theme", theme);
  }, [theme]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (loading) {
      // Если запрос уже в процессе, игнорируем дополнительные нажатия Enter
      return;
    }

    setError(null);
    setResult(null);

    const trimmed = url.trim();
    if (!trimmed) {
      setError("Введите URL, например: кейта.рф или https://example.com");
      return;
    }

    const now = Date.now();
    if (nextAllowedTime && now < nextAllowedTime) {
      const remainingSec = Math.max(1, Math.ceil((nextAllowedTime - now) / 1000));
      setError(`Слишком частые запросы. Попробуйте снова через ${remainingSec} сек.`);
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE_URL}/api/links`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ url: trimmed })
      });

      if (!res.ok) {
        const text = await res.text();
        setError(text || "Ошибка при создании ссылки");
        // После любой попытки запроса включаем небольшой "кулдаун"
        setNextAllowedTime(Date.now() + 5000);
        return;
      }

      const data = (await res.json()) as CreateResponse;
      setResult(data);
      // Успешный запрос — тоже ставим кулдаун
      setNextAllowedTime(Date.now() + 5000);
    } catch (err) {
      console.error(err);
      setError("Не удалось связаться с сервером");
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    if (!result?.shortUrl) return;
    try {
      await navigator.clipboard.writeText(result.shortUrl);
      alert("Скопировано в буфер обмена");
    } catch {
      alert("Не удалось скопировать");
    }
  };

  const toggleTheme = () => {
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
  };

  return (
    <div className="page">
      <div className="theme-toggle-wrapper">
        <button
          type="button"
          className="theme-toggle"
          onClick={toggleTheme}
          aria-label="Переключить тему"
        >
          {theme === "dark" ? "🌙 Тёмная" : "☀️ Светлая"}
        </button>
      </div>

      <div className="card">
        <h1>Сокращатель ссылок + QR</h1>
        <p className="subtitle">
          Введите длинный URL (можно без http/https), а сервис вернёт короткую
          ссылку и QR-код. Домен в Unicode (например, кейта.рф) тоже корректно
          сокращается.
        </p>

        <form onSubmit={handleSubmit} className="form">
          <input
            type="text"
            placeholder="кейта.рф, example.com/long/url или https://example.com"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
          />
          <button type="submit" disabled={loading}>
            {loading ? "Обрабатываем..." : "Сократить"}
          </button>
        </form>

        {error && <div className="error">{error}</div>}

        {result && (
          <div className="result">
            <h2>Результат</h2>
            <p class="result">
              Короткая ссылка:{" "}
              <a href={result.shortUrl} target="_blank" rel="noreferrer">
                {result.shortUrl}
              </a>
            </p>
            <button className="copy-button" onClick={handleCopy}>Скопировать ссылку</button>

            <div className="qr">
              <h3>QR-код</h3>
              <img
                src={`data:image/png;base64,${result.qrCodeBase64}`}
                alt="QR-код"
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};