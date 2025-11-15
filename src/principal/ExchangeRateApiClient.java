package principal;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ExchangeRateApiClient {
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6";
    private final HttpClient http;
    private final Gson gson;
    private final String apiKey;

    public ExchangeRateApiClient(String apiKey) {
        this.http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        this.gson = new Gson();
        this.apiKey = apiKey;
    }

    /**
     * Intenta cargar la API key desde:
     * 1) Variable de entorno EXR_API_KEY
     * 2) Archivo config/config.properties (prop api_key)
     */
    public static String loadApiKeyOrNull() {
        String fromEnv = System.getenv("EXR_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv.trim();

        try (FileInputStream fis = new FileInputStream("config/config.properties")) {
            Properties p = new Properties();
            p.load(fis);
            String fromFile = p.getProperty("api_key");
            if (fromFile != null && !fromFile.isBlank()) return fromFile.trim();
        } catch (Exception ignored) {
            // Si no existe el archivo se ignora; el caller debe manejar null
        }
        return null;
    }

    /**
     * Obtiene la conversion_rate entre base y target.
     * Retorna Optional<Double> vacio si la respuesta no contiene conversion_rate.
     */
    public Optional<Double> fetchPairRate(String base, String target) throws IOException, InterruptedException {
        String url = String.format("%s/%s/pair/%s/%s", BASE_URL, apiKey, base, target);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("Accept", "application/json").build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() / 100 != 2) {
            throw new IOException("HTTP error: " + resp.statusCode() + " - " + resp.body());
        }

        JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
        String result = root.has("result") ? root.get("result").getAsString() : null;

        if ("error".equalsIgnoreCase(result)) {
            String errorType = root.has("error-type") ? root.get("error-type").getAsString() : "unknown-error";
            throw new IOException("API error: " + errorType);
        }

        if (root.has("conversion_rate")) {
            return Optional.of(root.get("conversion_rate").getAsDouble());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Asíncrono — devuelve CompletableFuture con la tasa (ó excepción).
     */
    public CompletableFuture<Double> fetchPairRateAsync(String base, String target) {
        String url = String.format("%s/%s/pair/%s/%s", BASE_URL, apiKey, base, target);

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("Accept", "application/json").build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(resp -> {
            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("HTTP error: " + resp.statusCode());
            }
            JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
            String result = root.has("result") ? root.get("result").getAsString() : null;
            if ("error".equalsIgnoreCase(result)) {
                String errorType = root.has("error-type") ? root.get("error-type").getAsString() : "unknown-error";
                throw new RuntimeException("API error: " + errorType);
            }
            if (root.has("conversion_rate")) {
                return root.get("conversion_rate").getAsDouble();
            } else {
                throw new RuntimeException("conversion_rate not found in response");
            }
        });
    }

    public Optional<Map<String, Double>> fetchLatestRates(String base) {
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/" + base;

        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("Accept", "application/json").build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                System.err.println("Error HTTP: " + resp.statusCode());
                return Optional.empty();
            }

            JsonObject json = gson.fromJson(resp.body(), JsonObject.class);

            if (!json.has("conversion_rates")) {
                System.err.println("JSON no contiene conversion_rates");
                return Optional.empty();
            }

            JsonObject ratesObj = json.getAsJsonObject("conversion_rates");
            Map<String, Double> rates = new HashMap<>();

            for (String key : ratesObj.keySet()) {
                rates.put(key, ratesObj.get(key).getAsDouble());
            }

            return Optional.of(rates);

        } catch (Exception e) {
            System.err.println("Error al consultar /latest: " + e.getMessage());
            return Optional.empty();
        }
    }

}
